package com.azkari.wasalati

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranRepository(
    private val context: Context,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val rootDir by lazy { File(context.filesDir, "quran").apply { mkdirs() } }
    private val textFile by lazy { File(rootDir, "quran_text.json") }
    private val audioRoot by lazy { File(rootDir, "audio").apply { mkdirs() } }
    @Volatile
    private var cachedSurahs: List<CachedSurah>? = null

    suspend fun isTextDownloaded(): Boolean = withContext(Dispatchers.IO) { textFile.exists() }

    suspend fun ensureTextDownloaded(onProgress: (Int, Int) -> Unit = { _, _ -> }): Boolean {
        return withContext(Dispatchers.IO) {
            if (textFile.exists()) {
                if (cachedSurahs == null) cachedSurahs = parseCachedSurahs(JSONArray(textFile.readText()))
                return@withContext true
            }

            val request = Request.Builder()
                .url("https://api.alquran.cloud/v1/quran/quran-uthmani")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to download Quran text: ${response.code}")
                }
                val body = response.body?.string().orEmpty()
                val root = JSONObject(body)
                val surahs = root.getJSONObject("data").getJSONArray("surahs")
                val simplified = JSONArray()

                for (index in 0 until surahs.length()) {
                    val surah = surahs.getJSONObject(index)
                    simplified.put(
                        JSONObject()
                            .put("surahNum", surah.getInt("number"))
                            .put("name", surah.getString("name"))
                            .put("englishName", surah.optString("englishName"))
                            .put("revelationType", surah.optString("revelationType"))
                            .put("ayahs", simplifyAyahs(surah.getJSONArray("ayahs"))),
                    )
                    onProgress(index + 1, surahs.length())
                }

                textFile.writeText(simplified.toString())
                cachedSurahs = parseCachedSurahs(simplified)
                true
            }
        }
    }

    suspend fun surahMetadata(): List<QuranSurahMeta> = withContext(Dispatchers.IO) {
        loadSurahs().map {
            QuranSurahMeta(
                number = it.surahNum,
                name = it.name,
                englishName = it.englishName,
                ayahs = it.ayahs.size,
                type = it.revelationType,
            )
        }
    }

    suspend fun page(pageNum: Int): Pair<Int?, List<QuranPageGroup>> = withContext(Dispatchers.IO) {
        val groups = mutableListOf<QuranPageGroup>()
        var firstJuz: Int? = null

        loadSurahs().forEach { surah ->
            val ayahsOnPage = surah.ayahs.filter { it.page == pageNum }
            if (ayahsOnPage.isEmpty()) return@forEach
            if (firstJuz == null) firstJuz = ayahsOnPage.first().juz

            val firstAyah = ayahsOnPage.firstOrNull()
            val basmalaSplit = if (firstAyah != null && firstAyah.number == 1 && surah.surahNum != 1 && surah.surahNum != 9) {
                splitBasmala(firstAyah.text)
            } else {
                null
            }

            val displayAyahs = ayahsOnPage.map { ayah ->
                if (ayah.number == 1 && basmalaSplit?.second != null) {
                    ayah.copy(text = basmalaSplit.second)
                } else {
                    ayah
                }
            }

            groups += QuranPageGroup(
                surahNum = surah.surahNum,
                surahName = surah.name,
                basmalaText = basmalaSplit?.first,
                ayahs = displayAyahs,
            )
        }

        firstJuz to groups
    }

    suspend fun search(query: String): List<QuranSurahMeta> = withContext(Dispatchers.IO) {
        val normalized = normalizeQuery(query)
        if (normalized.isBlank()) return@withContext emptyList()
        surahMetadata()
            .filter {
                normalizeQuery(it.name).contains(normalized) ||
                    it.englishName.lowercase().contains(normalized) ||
                    it.number.toString() == query.trim()
            }
            .take(7)
    }

    suspend fun firstPageOfSurah(surahNumber: Int): Int? = withContext(Dispatchers.IO) {
        loadSurahs().firstOrNull { it.surahNum == surahNumber }?.ayahs?.firstOrNull()?.page
    }

    suspend fun resolveAudio(reciter: QuranReciter, surahNumber: Int): QuranAudioDescriptor = withContext(Dispatchers.IO) {
        val localDir = File(audioRoot, reciter.id.toString()).apply { mkdirs() }
        val audioFile = File(localDir, "$surahNumber.mp3")
        val timestampsFile = File(localDir, "$surahNumber.timestamps.json")

        if (audioFile.exists() && timestampsFile.exists()) {
            return@withContext QuranAudioDescriptor(
                uri = audioFile.absolutePath,
                isLocalFile = true,
                timestamps = parseTimestamps(JSONArray(timestampsFile.readText())),
            )
        }

        val metadata = fetchAudioMetadata(reciter, surahNumber)
        QuranAudioDescriptor(
            uri = metadata.first,
            isLocalFile = false,
            timestamps = metadata.second,
        )
    }

    suspend fun downloadedSurahs(reciter: QuranReciter): Set<Int> = withContext(Dispatchers.IO) {
        val dir = File(audioRoot, reciter.id.toString())
        if (!dir.exists()) return@withContext emptySet()
        dir.listFiles()
            .orEmpty()
            .filter { it.extension.equals("mp3", ignoreCase = true) }
            .mapNotNull { it.nameWithoutExtension.toIntOrNull() }
            .toSet()
    }

    suspend fun downloadAudioSurah(
        reciter: QuranReciter,
        surahNumber: Int,
        onProgress: (String?) -> Unit = {},
    ) {
        withContext(Dispatchers.IO) {
            val localDir = File(audioRoot, reciter.id.toString()).apply { mkdirs() }
            val audioFile = File(localDir, "$surahNumber.mp3")
            val timestampsFile = File(localDir, "$surahNumber.timestamps.json")

            if (audioFile.exists() && timestampsFile.exists()) return@withContext

            val (audioUrl, timestamps) = fetchAudioMetadata(reciter, surahNumber)
            val request = Request.Builder().url(audioUrl).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to download audio: ${response.code}")
                val body = response.body ?: throw IOException("Empty audio body")
                val contentLength = body.contentLength()
                val input = body.byteStream()
                audioFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { bytesRead = it } >= 0) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            val pct = ((totalRead * 100) / contentLength).toInt().coerceIn(0, 100)
                            onProgress("تحميل السورة $pct%")
                        }
                    }
                }
            }

            val array = JSONArray()
            timestamps.forEach { item ->
                array.put(
                    JSONObject()
                        .put("ayahIndex", item.ayahIndex)
                        .put("timestamp_from", item.startMs)
                        .put("timestamp_to", item.endMs),
                )
            }
            timestampsFile.writeText(array.toString())
        }
    }

    suspend fun downloadAllSurahs(
        reciter: QuranReciter,
        onProgress: (Int, Int, String) -> Unit,
    ) {
        val metadata = surahMetadata()
        val downloaded = downloadedSurahs(reciter)
        val pending = metadata.filter { !downloaded.contains(it.number) }
        var completed = 0
        pending.forEach { surah ->
            onProgress(completed, pending.size, "تحميل سورة ${surah.name}")
            downloadAudioSurah(reciter, surah.number)
            completed++
            onProgress(completed, pending.size, "تم تحميل ${surah.name}")
        }
    }

    suspend fun deleteDownloadedQuran() = withContext(Dispatchers.IO) {
        textFile.delete()
        audioRoot.deleteRecursively()
        audioRoot.mkdirs()
        cachedSurahs = null
    }

    private suspend fun loadSurahs(): List<CachedSurah> = withContext(Dispatchers.IO) {
        cachedSurahs?.let { return@withContext it }
        if (!textFile.exists()) return@withContext emptyList()
        val parsed = parseCachedSurahs(JSONArray(textFile.readText()))
        cachedSurahs = parsed
        parsed
    }

    private fun simplifyAyahs(source: JSONArray): JSONArray {
        val array = JSONArray()
        for (index in 0 until source.length()) {
            val ayah = source.getJSONObject(index)
            array.put(
                JSONObject()
                    .put("number", ayah.getInt("numberInSurah"))
                    .put("text", ayah.getString("text"))
                    .put("page", ayah.getInt("page"))
                    .put("juz", ayah.getInt("juz")),
            )
        }
        return array
    }

    private fun parseCachedSurahs(array: JSONArray): List<CachedSurah> {
        return List(array.length()) { index ->
            val surah = array.getJSONObject(index)
            val ayahArray = surah.getJSONArray("ayahs")
            CachedSurah(
                surahNum = surah.getInt("surahNum"),
                name = surah.getString("name"),
                englishName = surah.optString("englishName"),
                revelationType = surah.optString("revelationType"),
                ayahs = List(ayahArray.length()) { ayahIndex ->
                    val ayah = ayahArray.getJSONObject(ayahIndex)
                    QuranAyah(
                        number = ayah.getInt("number"),
                        text = ayah.getString("text"),
                        page = ayah.getInt("page"),
                        juz = ayah.getInt("juz"),
                    )
                },
            )
        }
    }

    private fun splitBasmala(text: String): Pair<String, String>? {
        if (text.isBlank()) return null
        val basmala = "بسم الله الرحمن الرحيم"
        val normalizedText = normalizeQuery(text)
        if (!normalizedText.startsWith(normalizeQuery(basmala))) return null

        val index = text.indexOf(' ') // preserve a light split without complex grapheme logic
        if (index == -1) return basmala to text
        val literal = if (text.startsWith("بِسْمِ")) {
            text.substring(0, minOf(text.length, 39))
        } else {
            basmala
        }
        val remaining = text.removePrefix(literal).trim()
        return literal to if (remaining.isBlank()) text else remaining
    }

    private fun normalizeQuery(value: String): String {
        return value
            .replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "")
            .replace(Regex("[أإآٱ]"), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .trim()
            .lowercase()
    }

    private fun fetchAudioMetadata(reciter: QuranReciter, surahNumber: Int): Pair<String, List<QuranTimestamp>> {
        val request = Request.Builder()
            .url("https://api.quran.com/api/v4/chapter_recitations/${reciter.id}/$surahNumber?segments=true")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to fetch audio metadata: ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val root = JSONObject(body)
            val audioFile = root.optJSONObject("audio_file")
                ?: throw IOException("Missing audio metadata")
            val timestamps = parseTimestamps(audioFile.optJSONArray("timestamps") ?: JSONArray())
            return resolveAbsoluteUrl(audioFile.getString("audio_url")) to timestamps
        }
    }

    private fun resolveAbsoluteUrl(url: String): String {
        return when {
            url.startsWith("https://") || url.startsWith("http://") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "https://verses.quran.com$url"
            else -> "https://verses.quran.com/$url"
        }
    }

    private fun parseTimestamps(array: JSONArray): List<QuranTimestamp> {
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            QuranTimestamp(
                ayahIndex = index,
                startMs = item.optLong("timestamp_from"),
                endMs = item.optLong("timestamp_to"),
            )
        }
    }

    private data class CachedSurah(
        val surahNum: Int,
        val name: String,
        val englishName: String,
        val revelationType: String,
        val ayahs: List<QuranAyah>,
    )
}
