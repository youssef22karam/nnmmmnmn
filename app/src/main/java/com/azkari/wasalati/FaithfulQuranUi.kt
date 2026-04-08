package com.azkari.wasalati

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.withStyle

@Composable
fun FaithfulQuranDialog(
    uiState: AppUiState2,
    viewModel: FaithfulMainViewModel,
) {
    Dialog(
        onDismissRequest = viewModel::dismissModal,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to QuranInk,
                            0.30f to Color(0xFF1A1207),
                            1.0f to QuranInk,
                        ),
                    ),
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                QuranModalHeader(onDismiss = viewModel::dismissModal)

                when (uiState.quran.mode) {
                    QuranReaderMode.DOWNLOAD -> QuranDownloadView(uiState.quran, viewModel)
                    else -> QuranChromeScaffold(quran = uiState.quran, viewModel = viewModel) {
                        when (uiState.quran.mode) {
                            QuranReaderMode.READER -> QuranReaderView(uiState.quran, viewModel)
                            QuranReaderMode.SURAH_LIST -> QuranSurahListView(uiState.quran, viewModel)
                            QuranReaderMode.SETTINGS -> QuranSettingsView(uiState.quran, viewModel)
                            QuranReaderMode.ACHIEVEMENTS -> QuranAchievementsView(uiState.quran, viewModel)
                            QuranReaderMode.RECITERS -> QuranRecitersView(uiState.quran, viewModel)
                            QuranReaderMode.DOWNLOAD -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranModalHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xCC1A1207))
            .border(1.dp, Color(0x1FD4A853))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "📖 القرآن الكريم",
            style = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldBright),
            modifier = Modifier.weight(1f),
        )
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color(0x22D4A853),
            modifier = Modifier.clickable(onClick = onDismiss),
        ) {
            Icon(Icons.Rounded.Close, contentDescription = null, tint = GoldBright, modifier = Modifier.padding(8.dp).size(18.dp))
        }
    }
}

@Composable
private fun QuranChromeScaffold(
    quran: QuranUiState,
    viewModel: FaithfulMainViewModel,
    content: @Composable () -> Unit,
) {
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    val pct = ((quran.currentPage - 1).coerceIn(0, 603) / 603f).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xE60A0703))
                .border(1.dp, Color(0x1FD4A853)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x22D4A853),
                    modifier = Modifier
                        .border(1.dp, Color(0x1FD4A853), RoundedCornerShape(12.dp))
                        .clickable { viewModel.showQuranMode(QuranReaderMode.SURAH_LIST) },
                ) {
                    Row(modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = GoldBright, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("السور", color = GoldBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الصفحة ${quran.currentPage}",
                        style = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldBright),
                    )
                    if (quran.dailyGoal > 0) {
                        Text(
                            text = "${quran.dailyLog.pagesRead.size}/${quran.dailyGoal} اليوم",
                            color = Color(0xFF8B7355).copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuranChromeIcon(icon = Icons.Rounded.Search, onClick = {
                        viewModel.showQuranMode(QuranReaderMode.READER)
                        searchOpen = !searchOpen
                    })
                    QuranChromeIcon(icon = Icons.Rounded.GraphicEq, onClick = { viewModel.showQuranMode(QuranReaderMode.RECITERS) })
                    QuranChromeIcon(icon = Icons.Rounded.Settings, onClick = { viewModel.showQuranMode(QuranReaderMode.SETTINGS) })
                    QuranChromeIcon(icon = Icons.Rounded.EmojiEvents, onClick = { viewModel.showQuranMode(QuranReaderMode.ACHIEVEMENTS) })
                }
            }

            AnimatedVisibility(searchOpen && quran.mode == QuranReaderMode.READER) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Column {
                        OutlinedTextField(
                            value = quran.searchQuery,
                            onValueChange = viewModel::updateQuranSearchQuery,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("ابحث عن سورة...", color = GoldBright.copy(alpha = 0.55f)) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = GoldBright) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GoldBright,
                                unfocusedTextColor = GoldBright,
                                focusedContainerColor = Color(0x12D4A853),
                                unfocusedContainerColor = Color(0x12D4A853),
                                focusedBorderColor = Color(0x4DD4A853),
                                unfocusedBorderColor = Color(0x33D4A853),
                                cursorColor = GoldBright,
                            ),
                            shape = RoundedCornerShape(14.dp),
                        )

                        if (quran.searchResults.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF14100A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0x33D4A853), RoundedCornerShape(14.dp)),
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                                    items(quran.searchResults) { surah ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.jumpToSurah(surah.number) }
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(surah.number.toString(), color = GoldLight.copy(alpha = 0.6f), modifier = Modifier.width(32.dp))
                                            Text(
                                                surah.name,
                                                color = GoldBright,
                                                style = TextStyle(fontFamily = AmiriFamily, fontSize = 18.sp),
                                                modifier = Modifier.weight(1f),
                                            )
                                            Text("${surah.ayahs} آية", color = GoldLight.copy(alpha = 0.35f), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0x14D4A853))) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct)
                        .height(2.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF6B4C00), GoldBright, Color(0xFFF5D78E)))),
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun QuranChromeIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x22D4A853),
        modifier = Modifier
            .size(32.dp)
            .clickable(onClick = onClick)
            .border(1.dp, Color(0x1FD4A853), RoundedCornerShape(12.dp)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = GoldBright, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun QuranDownloadView(
    quran: QuranUiState,
    viewModel: FaithfulMainViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📖", fontSize = 54.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "القرآن الكريم",
            style = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = GoldBright),
        )
        Spacer(Modifier.height(8.dp))
        Text("حمّل نص القرآن للقراءة بدون إنترنت", color = GoldLight.copy(alpha = 0.8f))
        Spacer(Modifier.height(4.dp))
        Text("الحجم التقريبي: 3 ميجابايت", color = GoldLight.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(22.dp))
        OutlinedButton(onClick = viewModel::downloadQuranText, enabled = !quran.download.isDownloading) {
            Icon(Icons.Rounded.Download, contentDescription = null, tint = GoldBright)
            Spacer(Modifier.width(8.dp))
            Text(if (quran.download.isDownloading) "جاري التحميل..." else "تحميل القرآن الكريم", color = GoldBright)
        }
        Spacer(Modifier.height(14.dp))
        if (quran.download.isDownloading) {
            Text("${quran.download.current}/${quran.download.total}", color = GoldLight)
        }
        quran.download.error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = Color(0xFFFCA5A5))
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 5, 10, 20).forEach { goal ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (quran.dailyGoal == goal) GoldBright else Color(0x22D4A853),
                    modifier = Modifier.clickable { viewModel.setQuranDailyGoal(goal) },
                ) {
                    Text(
                        goal.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = if (quran.dailyGoal == goal) QuranInk else GoldBright,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuranReaderView(
    quran: QuranUiState,
    viewModel: FaithfulMainViewModel,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            QuranMushafPage(
                quran = quran,
                modifier = Modifier.fillMaxSize(),
            )
        }
        QuranAudioBar(quran, viewModel)
        QuranNavBar(
            onPrev = viewModel::prevQuranPage,
            onMarkRead = viewModel::markCurrentQuranPageRead,
            onNext = viewModel::nextQuranPage,
        )
    }
}

@Composable
private fun QuranMushafPage(
    quran: QuranUiState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val charCount = quran.pageGroups.sumOf { group ->
            group.surahName.length + (group.basmalaText?.length ?: 0) + group.ayahs.sumOf { ayah -> ayah.text.length + 6 }
        }
        val pageText = buildQuranPageAnnotatedText(quran)
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current

        val baseFontScale = when {
            charCount > 1500 -> 17.8f
            charCount > 1300 -> 18.8f
            charCount > 1100 -> 19.8f
            charCount > 900 -> 21f
            else -> 22.4f
        }
        val baseLineScale = when {
            charCount > 1450 -> 1.84f
            charCount > 1250 -> 1.9f
            charCount > 1050 -> 1.98f
            else -> 2.06f
        }
        val horizontalPagePadding = if (maxWidth < 320.dp) 8.dp else 10.dp
        val verticalPagePadding = if (maxHeight < 520.dp) 4.dp else 6.dp
        val textWidthPx = with(density) { (maxWidth - (horizontalPagePadding * 2)).roundToPx().coerceAtLeast(1) }
        val textHeightPx = with(density) { (maxHeight - (verticalPagePadding * 2)).roundToPx().coerceAtLeast(1) }
        val pageTextStyle = remember(quran.currentPage, textWidthPx, textHeightPx, charCount) {
            resolveMushafPageTextStyle(
                textMeasurer = textMeasurer,
                pageText = pageText,
                widthPx = textWidthPx,
                heightPx = textHeightPx,
                baseFontSize = baseFontScale,
                baseLineScale = baseLineScale,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPagePadding, vertical = verticalPagePadding),
        ) {
            Text(
                text = pageText,
                modifier = Modifier.fillMaxSize(),
                style = pageTextStyle,
            )
        }
    }
}

private fun buildQuranPageAnnotatedText(quran: QuranUiState): AnnotatedString {
    return buildAnnotatedString {
        quran.pageGroups.forEachIndexed { groupIndex, group ->
            if (group.ayahs.firstOrNull()?.number == 1) {
                if (groupIndex > 0) {
                    append("\n\n")
                }
                withStyle(ParagraphStyle(textAlign = TextAlign.Center)) {
                    pushStyle(
                        SpanStyle(
                            color = Color(0xFFD4A853),
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = GoldBright.copy(alpha = 0.22f),
                                offset = Offset.Zero,
                                blurRadius = 10f,
                            ),
                        ),
                    )
                    append("سورة ${group.surahName}")
                    pop()
                    append("\n")
                }

                group.basmalaText?.let {
                    withStyle(ParagraphStyle(textAlign = TextAlign.Center)) {
                        pushStyle(
                            SpanStyle(
                                color = GoldBright.copy(alpha = 0.65f),
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        append(it)
                        pop()
                        append("\n")
                    }
                }
            } else if (groupIndex > 0) {
                append("\n")
            }

            group.ayahs.forEach { ayah ->
                val isHighlighted =
                    quran.audio.currentSurah == group.surahNum && quran.audio.currentAyahIndex + 1 == ayah.number

                pushStyle(
                    SpanStyle(
                        color = if (isHighlighted) Color(0xFFF5D48A) else Color(0xFFC8A24A),
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                        shadow = if (isHighlighted) {
                            Shadow(
                                color = GoldBright.copy(alpha = 0.95f),
                                offset = Offset.Zero,
                                blurRadius = 18f,
                            )
                        } else {
                            null
                        },
                    ),
                )
                append(ayah.text)
                pop()

                append(" ")
                pushStyle(
                    SpanStyle(
                        color = if (isHighlighted) {
                            Color(0xFFF5D48A)
                        } else {
                            GoldBright.copy(alpha = 0.38f)
                        },
                        baselineShift = BaselineShift.Superscript,
                        shadow = if (isHighlighted) {
                            Shadow(
                                color = GoldBright.copy(alpha = 0.72f),
                                offset = Offset.Zero,
                                blurRadius = 12f,
                            )
                        } else {
                            null
                        },
                    ),
                )
                append("﴿${ayah.number}﴾")
                pop()
                append(" ")
            }
        }
    }
}

@Composable
private fun QuranNavBar(
    onPrev: () -> Unit,
    onMarkRead: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(
        color = Color(0xE60A0703),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x1FD4A853)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x14D4A853),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0x1FD4A853), RoundedCornerShape(12.dp))
                    .clickable(onClick = onPrev),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("السابقة", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1.2f)
                    .clickable(onClick = onMarkRead),
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(GoldBright, Color(0xFFB8860B))))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓ أنهيت الصفحة", color = Color(0xFF1A1207), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x14D4A853),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0x1FD4A853), RoundedCornerShape(12.dp))
                    .clickable(onClick = onNext),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("التالية", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun QuranSurahListView(
    quran: QuranUiState,
    viewModel: FaithfulMainViewModel,
) {
    if (quran.surahs.isEmpty()) {
        QuranEmptyState("حمّل القرآن أولًا أو انتظر تجهيز الفهرس.")
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        quran.audio.bulkDownloadLabel?.let { StatusBanner(it) }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(quran.surahs) { surah ->
                val downloaded = quran.audio.downloadedSurahs.contains(surah.number)
                val isCurrent = quran.audio.currentSurah == surah.number
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isCurrent) Color(0x22D4A853) else Color(0xFF14100A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.jumpToSurah(surah.number) },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x22D4A853),
                            ) {
                                Text(
                                    text = surah.number.toString(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    color = GoldBright,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = surah.name,
                                    style = TextStyle(
                                        fontFamily = AmiriFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = GoldBright,
                                    ),
                                )
                                Text(
                                    text = "${surah.englishName} • ${surah.ayahs} آية • ${surahTypeLabel(surah.type)}",
                                    color = GoldLight.copy(alpha = 0.72f),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.playSurah(surah.number) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = GoldBright)
                                Spacer(Modifier.width(6.dp))
                                Text("استماع", color = GoldBright)
                            }
                            OutlinedButton(
                                onClick = {
                                    if (!downloaded) {
                                        viewModel.downloadAudioSurah(surah.number)
                                    } else {
                                        viewModel.jumpToSurah(surah.number)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    if (downloaded) Icons.Rounded.CheckCircle else Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = GoldBright,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (downloaded) "محمّلة" else "تحميل", color = GoldBright)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranSettingsView(
    quran: QuranUiState,
    viewModel: FaithfulMainViewModel,
) {
    val goalOptions = listOf(0, 2, 5, 10, 20)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            QuranSettingsCard(title = "الورد اليومي") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    goalOptions.forEach { goal ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (quran.dailyGoal == goal) GoldBright else Color(0x22D4A853),
                            modifier = Modifier.clickable { viewModel.setQuranDailyGoal(goal) },
                        ) {
                            Text(
                                text = if (goal == 0) "بدون" else "$goal صفحات",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                color = if (quran.dailyGoal == goal) QuranInk else GoldBright,
                            )
                        }
                    }
                }
                Text(
                    text = "قرأت اليوم ${quran.dailyLog.pagesRead.size} صفحة.",
                    color = GoldLight.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        item {
            QuranSettingsCard(title = "التلاوة والصوت") {
                Text(
                    text = "القارئ الحالي: ${quran.selectedReciter.name}",
                    color = GoldBright,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "السور المحمّلة صوتيًا: ${quran.audio.downloadedSurahs.size}",
                    color = GoldLight.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.showQuranMode(QuranReaderMode.RECITERS) }) {
                        Text("اختيار قارئ", color = GoldBright)
                    }
                    OutlinedButton(onClick = viewModel::downloadAllSurahs) {
                        Icon(Icons.Rounded.Download, contentDescription = null, tint = GoldBright)
                        Spacer(Modifier.width(6.dp))
                        Text("تحميل الكل", color = GoldBright)
                    }
                }
                quran.audio.bulkDownloadLabel?.let {
                    Text(it, color = GoldLight.copy(alpha = 0.75f))
                }
            }
        }

        item {
            QuranSettingsCard(title = "الموضع الحالي") {
                Text(
                    text = "الصفحة الحالية: ${quran.currentPage}",
                    color = GoldBright,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "يمكنك العودة دائمًا إلى موضعك الأخير داخل القارئ.",
                    color = GoldLight.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(onClick = { viewModel.showQuranMode(QuranReaderMode.READER) }) {
                    Text("العودة إلى القراءة", color = GoldBright)
                }
            }
        }
    }
}

@Composable
private fun QuranAchievementsView(
    quran: QuranUiState,
    viewModel: FaithfulMainViewModel,
) {
    val maxPages = (quran.stats.dayBars.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuranMetricCard("السلسلة", "${quran.stats.streakDays}", Modifier.weight(1f))
                QuranMetricCard("الصفحات", "${quran.stats.totalPagesLast30Days}", Modifier.weight(1f))
                QuranMetricCard("أيام القراءة", "${quran.stats.daysReadLast30Days}", Modifier.weight(1f))
            }
        }

        item {
            Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF14100A)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("آخر 30 يوم", color = GoldBright, fontWeight = FontWeight.Bold)
                    if (quran.stats.dayBars.isEmpty()) {
                        Text("ابدأ القراءة ليظهر التقدّم هنا.", color = GoldLight.copy(alpha = 0.72f))
                    } else {
                        quran.stats.dayBars.takeLast(14).forEach { (label, pages) ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(label, color = GoldLight.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
                                    Text("$pages صفحة", color = GoldBright, style = MaterialTheme.typography.bodySmall)
                                }
                                Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.08f)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(pages / maxPages.toFloat())
                                            .height(8.dp)
                                            .background(GoldBright),
                                    )
                                }
                            }
                        }
                    }
                    OutlinedButton(onClick = { viewModel.showQuranMode(QuranReaderMode.READER) }) {
                        Text("العودة إلى القراءة", color = GoldBright)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranRecitersView(
    quran: QuranUiState,
    viewModel: FaithfulMainViewModel,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(quran.reciters) { reciter ->
            val isSelected = reciter.id == quran.selectedReciter.id
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (isSelected) Color(0x22D4A853) else Color(0xFF14100A),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectReciter(reciter)
                        viewModel.showQuranMode(
                            if (quran.download.isDownloaded) QuranReaderMode.READER else QuranReaderMode.DOWNLOAD,
                        )
                    },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reciter.name, color = GoldBright, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isSelected) "القارئ المحدد حاليًا" else "اضغط للتبديل إليه",
                            color = GoldLight.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (isSelected) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = GoldBright)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranAudioBar(
    quran: QuranUiState,
    viewModel: FaithfulMainViewModel,
) {
    val targetSurah = quran.audio.currentSurah.takeIf { it > 0 } ?: return
    val targetMeta = quran.surahs.firstOrNull { it.number == targetSurah }
    val label = quran.audio.label.ifBlank {
        if (targetMeta == null) "استمع إلى التلاوة" else "سورة ${targetMeta.name}"
    }

    Surface(
        color = Color(0xF2080602),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x2ED4A853)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                label,
                color = GoldBright,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontFamily = AmiriFamily, fontSize = 13.sp),
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )

            NowPlayingSquareButton(icon = Icons.Rounded.SkipPrevious, onClick = viewModel::prevAyahInQueue)
            NowPlayingPlayButton(
                icon = if (quran.audio.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                onClick = viewModel::toggleReaderPlayPause,
            )
            NowPlayingSquareButton(icon = Icons.Rounded.SkipNext, onClick = viewModel::skipNextSurah)
        }
    }
}

@Composable
private fun NowPlayingSquareButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0x1AD4A853),
        modifier = Modifier
            .size(30.dp)
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = GoldBright, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun NowPlayingPlayButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        shadowElevation = 3.dp,
        modifier = Modifier
            .size(34.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(GoldBright, Color(0xFFB8860B))))
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1A1207), modifier = Modifier.size(18.dp))
        }
    }
}

private fun resolveMushafPageTextStyle(
    textMeasurer: TextMeasurer,
    pageText: AnnotatedString,
    widthPx: Int,
    heightPx: Int,
    baseFontSize: Float,
    baseLineScale: Float,
): TextStyle {
    var currentFontSize = baseFontSize
    var currentLineScale = baseLineScale
    var currentStyle = mushafPageTextStyle(currentFontSize, currentLineScale)

    while (currentFontSize > 10.2f) {
        val layoutResult = textMeasurer.measure(
            text = pageText,
            style = currentStyle,
            constraints = Constraints(maxWidth = widthPx, maxHeight = heightPx),
        )
        if (!layoutResult.hasVisualOverflow) {
            return currentStyle
        }

        currentFontSize -= 0.3f
        currentLineScale = (currentLineScale - 0.03f).coerceAtLeast(1.56f)
        currentStyle = mushafPageTextStyle(currentFontSize, currentLineScale)
    }

    return currentStyle
}

private fun mushafPageTextStyle(
    fontSize: Float,
    lineScale: Float,
): TextStyle = TextStyle(
    fontFamily = AmiriFamily,
    fontSize = fontSize.sp,
    lineHeight = (fontSize * lineScale).sp,
    color = Color(0xFFC8A24A),
    textAlign = TextAlign.Justify,
    shadow = Shadow(
        color = GoldBright.copy(alpha = 0.08f),
        offset = Offset.Zero,
        blurRadius = 3f,
    ),
)

@Composable
private fun QuranSettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF14100A)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, color = GoldBright, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun QuranMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = Color(0xFF14100A)) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, color = GoldLight.copy(alpha = 0.72f), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, color = GoldBright, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        }
    }
}

@Composable
private fun StatusBanner(text: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = Color(0x22D4A853)) {
        Text(
            text = text,
            color = GoldLight,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun QuranEmptyState(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = GoldLight)
    }
}

private fun surahTypeLabel(type: String): String {
    return when (type.lowercase()) {
        "meccan" -> "مكية"
        "medinan" -> "مدنية"
        else -> type
    }
}
