package com.azkari.wasalati

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AzkariFaithfulApp(
    viewModel: FaithfulMainViewModel,
    notificationPermissionGranted: Boolean,
    exactAlarmPermissionGranted: Boolean,
    onRequestNotifications: () -> Unit,
    onRequestExactAlarms: () -> Unit,
    onUseCurrentLocation: () -> Unit,
) {
    val uiState = viewModel.uiState
    AzkariFaithfulTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ForestLight.copy(alpha = 0.06f), Cream),
                            radius = 1400f,
                        ),
                    )
                    .testTag("app_root"),
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        HomeStickyHeader(
                            uiState = uiState,
                            onOpenSettings = { viewModel.openModal(AppModal.SETTINGS) },
                            onOpenQada = { viewModel.openModal(AppModal.QADA) },
                            onOpenCity = { viewModel.openModal(AppModal.CITY) },
                            onToggleBanner = viewModel::toggleBanner,
                            onOpenTracker = { viewModel.openModal(AppModal.TRACKER) },
                            onOpenQuran = viewModel::openQuran,
                            onSelectTab = viewModel::selectTab,
                        )
                    },
                ) { padding ->
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Forest)
                        }
                    } else {
                        HomeContent(
                            modifier = Modifier.padding(padding),
                            uiState = uiState,
                            viewModel = viewModel,
                        )
                    }
                }
                if (uiState.isOffline) {
                    OfflineBanner(Modifier.align(Alignment.TopCenter).padding(top = 92.dp))
                }
                uiState.currentToast?.let { toast ->
                    ToastCard(
                        message = toast.message,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .testTag("toast_card"),
                    )
                }
                FaithfulModalHost(
                    uiState = uiState,
                    viewModel = viewModel,
                    notificationPermissionGranted = notificationPermissionGranted,
                    exactAlarmPermissionGranted = exactAlarmPermissionGranted,
                    onRequestNotifications = onRequestNotifications,
                    onRequestExactAlarms = onRequestExactAlarms,
                    onUseCurrentLocation = onUseCurrentLocation,
                )
            }
        }
    }
}

// ─── Brand mark ───────────────────────────────────────────────────────────────
@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF052E16), Color(0xFF065F46)))),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val s = size.width / 32f
            val white12 = Color(0x1FFFFFFF)
            val white90 = Color(0xE6FFFFFF)
            val white85 = Color(0xD9FFFFFF)
            val white70 = Color(0xB3FFFFFF)
            val greenDoor = Color(0x80065F46)
            val greenWin = Color(0x59065F46)
            val gold = Color(0xFFF59E0B)
            val mqPath = Path().apply {
                moveTo(16f * s, 4f * s)
                cubicTo(13f * s, 4f * s, 11f * s, 6f * s, 11f * s, 8.5f * s)
                lineTo(11f * s, 11f * s); lineTo(9.5f * s, 11f * s)
                lineTo(8f * s, 13f * s); lineTo(4f * s, 13f * s)
                lineTo(4f * s, 27f * s); lineTo(28f * s, 27f * s)
                lineTo(28f * s, 13f * s); lineTo(24f * s, 13f * s)
                lineTo(22.5f * s, 11f * s); lineTo(21f * s, 11f * s)
                lineTo(21f * s, 8.5f * s)
                cubicTo(21f * s, 6f * s, 19f * s, 4f * s, 16f * s, 4f * s)
                close()
            }
            drawPath(mqPath, white12)
            drawOval(white90, Offset(11f * s, 5f * s), Size(10f * s, 10f * s))
            drawRect(white70, Offset(5f * s, 14f * s), Size(3.5f * s, 10f * s))
            drawOval(white85, Offset(4.75f * s, 11.5f * s), Size(4f * s, 4f * s))
            drawRect(white70, Offset(23.5f * s, 14f * s), Size(3.5f * s, 10f * s))
            drawOval(white85, Offset(23.25f * s, 11.5f * s), Size(4f * s, 4f * s))
            drawRect(white85, Offset(8f * s, 17f * s), Size(16f * s, 10f * s))
            val door = Path().apply {
                moveTo(14f * s, 27f * s); lineTo(14f * s, 22f * s)
                cubicTo(14f * s, 20.9f * s, 14.9f * s, 20f * s, 16f * s, 20f * s)
                cubicTo(17.1f * s, 20f * s, 18f * s, 20.9f * s, 18f * s, 22f * s)
                lineTo(18f * s, 27f * s); close()
            }
            drawPath(door, greenDoor)
            drawRoundRect(greenWin, Offset(10f * s, 19f * s), Size(2.5f * s, 2.5f * s), CornerRadius(0.8f * s))
            drawRoundRect(greenWin, Offset(19.5f * s, 19f * s), Size(2.5f * s, 2.5f * s), CornerRadius(0.8f * s))
            val crescent = Path().apply {
                moveTo(16f * s, 6.5f * s)
                cubicTo(14.5f * s, 6.5f * s, 13.5f * s, 7.8f * s, 13.5f * s, 9f * s)
                cubicTo(14.5f * s, 8.5f * s, 15.8f * s, 8.5f * s, 16.8f * s, 9.2f * s)
                cubicTo(16f * s, 7.8f * s, 16f * s, 6.5f * s, 16f * s, 6.5f * s)
                close()
            }
            drawPath(crescent, gold, style = Fill)
            val star = Path().apply {
                moveTo(17.5f * s, 5.5f * s); lineTo(17.8f * s, 6.4f * s)
                lineTo(18.7f * s, 6.4f * s); lineTo(18.0f * s, 7.0f * s)
                lineTo(18.3f * s, 7.9f * s); lineTo(17.5f * s, 7.3f * s)
                lineTo(16.7f * s, 7.9f * s); lineTo(17.0f * s, 7.0f * s)
                lineTo(16.3f * s, 6.4f * s); lineTo(17.2f * s, 6.4f * s)
                close()
            }
            drawPath(star, gold, style = Fill)
        }
    }
}

// ─── Header button ───────────────────────────────────────────────────────────
@Composable
private fun HdrButton(
    onClick: () -> Unit,
    tag: String = "",
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .size(30.dp)
            .then(if (tag.isNotEmpty()) Modifier.testTag(tag) else Modifier)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

// ─── City pill ───────────────────────────────────────────────────────────────
@Composable
private fun CityButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .height(30.dp)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(12.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151), maxLines = 1)
        }
    }
}

// ─── Sticky header — tighter vertical rhythm ─────────────────────────────────
@Composable
private fun HomeStickyHeader(
    uiState: AppUiState2,
    onOpenSettings: () -> Unit,
    onOpenQada: () -> Unit,
    onOpenCity: () -> Unit,
    onToggleBanner: () -> Unit,
    onOpenTracker: () -> Unit,
    onOpenQuran: () -> Unit,
    onSelectTab: (HomeTab) -> Unit,
) {
    Surface(color = Color(0xFFFDFCFA), shadowElevation = 10.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // ↓ vertical reduced from 8 → 5
                .padding(horizontal = 14.dp, vertical = 5.dp),
        ) {
            // ── Top row ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BrandMark()
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "أذكاري وصلاتي",
                        style = TextStyle(
                            brush = Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF10B981))),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                        ),
                        maxLines = 1,
                    )
                    Text(text = uiState.hijriDate, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF), maxLines = 1)
                }
                HdrButton(onClick = onOpenSettings, tag = "settings_button") {
                    Icon(Icons.Rounded.Tune, contentDescription = null, tint = Color(0xFF374151), modifier = Modifier.size(13.dp))
                }
                Box {
                    HdrButton(onClick = onOpenQada, tag = "qada_button") {
                        Icon(AccountBalanceMosqueIcon, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(13.dp))
                    }
                    if (uiState.qada.values.sum() > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(14.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFE11D48)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(uiState.qada.values.sum().toString(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                CityButton(label = uiState.currentCity?.name ?: "اختر مدينة", onClick = onOpenCity)
            }

            // ↓ reduced from 8 → 4
            Spacer(Modifier.height(4.dp))

            // ── Prayer banner ─────────────────────────────────────────────────
            if (uiState.currentCity != null && uiState.prayerSummary != null) {
                PrayerBanner(uiState = uiState, onToggle = onToggleBanner, onOpenTracker = onOpenTracker)
                // ↓ reduced from 5 → 3
                Spacer(Modifier.height(3.dp))
            }

            // ── Quran entry ───────────────────────────────────────────────────
            QuranEntryCard(onOpenQuran = onOpenQuran)

            // ── Daily Quran tracker ───────────────────────────────────────────
            if (uiState.quran.dailyGoal > 0) {
                // ↓ reduced from 5 → 3
                Spacer(Modifier.height(3.dp))
                QuranDailyTrackerCard(uiState.quran)
            }

            // ↓ reduced from 8 → 4
            Spacer(Modifier.height(4.dp))
            TabStrip(selected = uiState.selectedTab, onSelect = onSelectTab)
        }
    }
}

// ─── Prayer banner — tighter collapsed & expanded ────────────────────────────
@Composable
private fun PrayerBanner(
    uiState: AppUiState2,
    onToggle: () -> Unit,
    onOpenTracker: () -> Unit,
) {
    val summary = uiState.prayerSummary ?: return
    val collapsedRadius = 40.dp
    val expandedRadius = 18.dp
    val radius = if (uiState.bannerCollapsed) collapsedRadius else expandedRadius
    val shape = RoundedCornerShape(radius)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
            .testTag("prayer_banner"),
        shape = shape,
        color = Color.Transparent,
        shadowElevation = if (uiState.bannerCollapsed) 3.dp else 10.dp,
    ) {
        Box(modifier = Modifier.background(Brush.linearGradient(listOf(Color(0xFF0A1628), ForestDark, Color(0xFF064E3B))))) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .background(Brush.radialGradient(listOf(Color(0x2610B981), Color.Transparent))),
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .background(Brush.radialGradient(listOf(Color(0x1F3B82F6), Color.Transparent))),
            )

            Column {
                if (uiState.bannerCollapsed) {
                    // ── Collapsed — vertical padding reduced from 4 → 3 ──────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggle)
                            .padding(horizontal = 16.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(
                                text = "المتبقي للصلاة القادمة",
                                color = ForestLight.copy(alpha = 0.75f),
                                fontSize = 10.sp,          // ↓ 11 → 10
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = formatCountdown(summary.countdownMillis),
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, letterSpacing = 1.1.sp, color = Color.White), // ↓ 23 → 21
                            )
                            Text(
                                text = summary.nextPrayer.displayName(),
                                color = Color(0xFFC5F4D8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    // ── Expanded — grabber & compact bar ─────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp, bottom = 1.dp), // ↓ top 4 → 3
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggle)
                            .padding(horizontal = 12.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "المتبقي للصلاة القادمة", color = ForestLight.copy(alpha = 0.7f), fontSize = 7.sp, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formatCountdown(summary.countdownMillis),
                                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 0.8.sp, color = Color.White),
                                )
                                Spacer(Modifier.width(5.dp))
                                Row(modifier = Modifier.padding(bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("←", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                    Spacer(Modifier.width(2.dp))
                                    Text(summary.nextPrayer.displayName(), color = ForestLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                        PrayerDotsRow(uiState.prayerDots)
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                        }
                    }
                }

                // ── Expandable body — tighter padding & spacing ───────────────
                AnimatedVisibility(!uiState.bannerCollapsed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            // ↓ bottom 8 → 6, start/end 10 → 8
                            .padding(start = 8.dp, end = 8.dp, bottom = 6.dp),
                        // ↓ spacedBy 5 → 4
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "السنن والنوافل للصلاة القادمة",
                            color = ForestLight.copy(alpha = 0.85f),
                            fontSize = 10.sp,          // ↓ 11 → 10
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        SunnahStrip(summary.sunnahInfo)
                        PrayerGrid(uiState.prayerDots)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "متابعة الصلوات", color = ForestLight, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onOpenTracker))
                            Spacer(Modifier.weight(1f))
                            Text(text = "${uiState.todayDoneCount}/5 اليوم", color = Color.White.copy(alpha = 0.38f), fontSize = 9.sp)
                            if (uiState.streakDays >= 2) {
                                Spacer(Modifier.width(7.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFF97316))))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(text = "🔥 ${uiState.streakDays}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerDotsRow(dots: List<PrayerDotUi>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dots.forEach { dot ->
            val (bg, border) = when (dot.status) {
                PrayerDotStatus.DONE -> ForestLight to ForestLight
                PrayerDotStatus.MISSED -> Color(0xFFF43F5E) to Color(0xFFF43F5E)
                PrayerDotStatus.PENDING -> Color.White.copy(alpha = 0.13f) to Color.White.copy(alpha = 0.18f)
                PrayerDotStatus.UPCOMING -> Color.White.copy(alpha = 0.04f) to Color.White.copy(alpha = 0.07f)
            }
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(bg, androidx.compose.foundation.shape.CircleShape)
                    .border(1.5.dp, border, androidx.compose.foundation.shape.CircleShape),
            )
        }
    }
}

@Composable
private fun SunnahStrip(sunnahInfo: SunnahInfo) {
    val shape = RoundedCornerShape(14.dp)
    Surface(shape = shape, color = Color.Transparent, modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.09f), shape)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f))
                // ↓ vertical 4 → 3
                .padding(horizontal = 16.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("قبل", color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                Text(sunnahInfo.pre, color = Color.White, textAlign = TextAlign.Center, fontSize = 11.sp)
            }
            Box(modifier = Modifier.height(15.dp).width(1.dp).background(Color.White.copy(alpha = 0.15f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("بعد", color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                Text(sunnahInfo.post, color = Color.White, textAlign = TextAlign.Center, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PrayerGrid(dots: List<PrayerDotUi>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        dots.forEach { item ->
            val alpha = if (item.status == PrayerDotStatus.UPCOMING) 1f else 0.55f
            Surface(modifier = Modifier.weight(1f).alpha(alpha), shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.08f)) {
                Column(
                    // ↓ vertical 7 → 5
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(item.label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                    Text(item.timeLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.height(3.dp))
                    PrayerDotsRow(listOf(item))
                }
            }
        }
    }
}

// ─── Quran entry card ─────────────────────────────────────────────────────────
@Composable
private fun QuranEntryCard(onOpenQuran: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Surface(shape = shape, color = Color.Transparent, modifier = Modifier.fillMaxWidth().testTag("quran_entry_card")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.linearGradient(listOf(GoldBright.copy(alpha = 0.09f), GoldBright.copy(alpha = 0.02f))))
                .border(1.dp, GoldBright.copy(alpha = 0.25f), shape)
                .clickable(onClick = onOpenQuran)
                // ↓ vertical 9 → 7
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .border(1.dp, Color(0x80FFEDD5), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📖", fontSize = 13.sp)
                }
                Spacer(Modifier.width(9.dp))
                Text(text = "القرآن الكريم", style = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberText))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(0.7f)) {
                Text(text = "قراءة واستماع", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA38A64))
                Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = null, tint = Color(0xFFA38A64), modifier = Modifier.size(13.dp))
            }
        }
    }
}

// ─── Daily Quran tracker ─────────────────────────────────────────────────────
@Composable
private fun QuranDailyTrackerCard(quranUiState: QuranUiState) {
    val goal = quranUiState.dailyGoal.coerceAtLeast(1)
    val progress = (quranUiState.dailyLog.pagesRead.size.toFloat() / goal).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(16.dp)
    Surface(
        shape = shape,
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldBright.copy(alpha = 0.15f), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // ↓ vertical 3 → 2
                .padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = GoldBright.copy(alpha = 0.45f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(7.dp))
            Text("وِردك اليومي", modifier = Modifier.weight(1f), color = Color(0xFF8B7355), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "${quranUiState.dailyLog.pagesRead.size}/${quranUiState.dailyGoal}", fontWeight = FontWeight.Bold, color = Color(0xFFB48530), fontSize = 11.sp)
            Spacer(Modifier.width(9.dp))
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(GoldBright.copy(alpha = 0.15f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(5.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(GoldBright, Color(0xFFB48530)))),
                )
            }
            if (quranUiState.stats.streakDays >= 2) {
                Spacer(Modifier.width(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 10.sp)
                    Spacer(Modifier.width(2.dp))
                    Text(quranUiState.stats.streakDays.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB48530))
                }
            }
        }
    }
}

// ─── Tab strip ────────────────────────────────────────────────────────────────
@Composable
private fun TabStrip(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .testTag("tab_strip"),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        HomeTab.entries
            .filter { it != HomeTab.FRIDAY || Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY }
            .forEach { tab ->
                HomeTabPill(tab = tab, selected = selected == tab, onClick = { onSelect(tab) })
            }
    }
}

@Composable
private fun HomeTabPill(tab: HomeTab, selected: Boolean, onClick: () -> Unit) {
    val isFriday = tab == HomeTab.FRIDAY
    val shape = RoundedCornerShape(20.dp)
    val background = when {
        selected -> Forest
        isFriday -> Color(0xFFFFFBEB).copy(alpha = 0.7f)
        else -> Color.White.copy(alpha = 0.7f)
    }
    val border = when {
        selected -> Color.Transparent
        isFriday -> Color(0xFFFEF3C7).copy(alpha = 0.8f)
        else -> Color(0xFFF3F4F6).copy(alpha = 0.8f)
    }
    val labelColor = when {
        selected -> Color.White
        isFriday -> Color(0xFFB45309)
        else -> Color(0xFF6B7280)
    }
    Surface(
        shape = shape,
        color = background,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier
            .border(1.dp, border, shape)
            .clickable(onClick = onClick),
    ) {
        Text(
            tab.chipLabel,
            // ↓ vertical 5 → 4
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            maxLines = 1,
        )
    }
}

// ─── Home content ─────────────────────────────────────────────────────────────
@Composable
private fun HomeContent(
    modifier: Modifier,
    uiState: AppUiState2,
    viewModel: FaithfulMainViewModel,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        var previousScroll = 0
        snapshotFlow { (listState.firstVisibleItemIndex * 10_000) + listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collectLatest { currentScroll: Int ->
                val delta = currentScroll - previousScroll
                when {
                    currentScroll <= 8 -> viewModel.setBannerCollapsed(false)
                    delta > 18 -> viewModel.setBannerCollapsed(true)
                }
                if (delta > 2 || delta < -2) previousScroll = currentScroll
            }
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_list"),
        state = listState,
        // ↓ top 8 → 6
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 120.dp),
        // ↓ reduced from 12 → 7 for tighter non-azkar spacing; azkar cards share this
        //   but their internal card design is unchanged
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        item {
            if (uiState.currentCity == null || uiState.currentPrayerTimes == null || uiState.prayerSummary == null) {
                EmptyCityCard(onOpenCity = { viewModel.openModal(AppModal.CITY) })
            }
        }
        uiState.suggestion?.let { suggestion ->
            item {
                FaithfulSuggestionCard(suggestion = suggestion, onReset = viewModel::resetAutoTab)
            }
        }
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            item { FridayKahfCard(onOpen = viewModel::openSurahKahf) }
        }
        if (uiState.homeSections.isEmpty()) {
            item { EmptyStateCard() }
        } else {
            uiState.homeSections.forEach { section ->
                if (section.title != null) {
                    item(key = "${section.id}_divider") { SectionDivider(section) }
                }
                section.items.forEachIndexed { index, item ->
                    item(key = "${section.id}_$index") {
                        AzkarItemCard(
                            section = section,
                            index = index,
                            item = item,
                            remaining = viewModel.itemRemaining["${section.id}:$index"] ?: item.count,
                            onCount = { viewModel.decrementAzkar(section.id, index, item.count) },
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = "﷽ — صدقة جارية للحاج ماهر كرم وجميع أموات المسلمين رحمهم الله",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

// ─── Empty city card ──────────────────────────────────────────────────────────
@Composable
private fun EmptyCityCard(onOpenCity: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().testTag("home_empty_state"),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Forest, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text("ابدأ باختيار مدينتك", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text("اختر مدينتك لعرض مواقيت الصلاة والأذكار المناسبة.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onOpenCity) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("اختيار مدينة", fontSize = 13.sp)
            }
        }
    }
}

// ─── Suggestion card — minimal vertical padding ───────────────────────────────
@Composable
private fun FaithfulSuggestionCard(suggestion: String, onReset: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Surface(shape = shape, color = Color.Transparent, modifier = Modifier.fillMaxWidth().border(1.dp, Sage, shape)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFECFDF5).copy(alpha = 0.8f))
                // ↓ vertical kept at 1 — already minimal
                .padding(horizontal = 12.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⏱️", fontSize = 12.sp)
            Spacer(Modifier.width(7.dp))
            Text(suggestion, modifier = Modifier.weight(1f), color = Color(0xFF065F46), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(9.dp))
                    .clickable(onClick = onReset),
            ) {
                Text("تلقائي", modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestDark)
            }
        }
    }
}

// ─── Friday Kahf card ─────────────────────────────────────────────────────────
@Composable
private fun FridayKahfCard(onOpen: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Surface(shape = shape, color = Color.Transparent, modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x38FBBF24), shape), shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFF422006), Color(0xFF78350F), Color(0xFF92400E))))
                // ↓ vertical 12 → 8
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)       // ↓ 34 → 32
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FBBF24)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🕌", fontSize = 15.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("يوم الجمعة المبارك", color = Color(0xFFFBBF24), fontWeight = FontWeight.ExtraBold, style = TextStyle(fontFamily = AmiriFamily))
                Text("«من قرأ سورة الكهف يوم الجمعة أضاء له النور ما بين الجمعتين»", color = Color(0xBFFDE68A), fontSize = 10.sp, lineHeight = 15.sp)
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x2EFBBF24),
                modifier = Modifier
                    .border(1.dp, Color(0x4DFBBF24), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpen),
            ) {
                Row(
                    // ↓ vertical 8 → 6
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("📖", fontSize = 11.sp)
                    Spacer(Modifier.width(5.dp))
                    Text("اقرأ الكهف", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ─── Section divider — reduced height + tighter icon ─────────────────────────
@Composable
private fun SectionDivider(section: AzkarSection) {
    val colors = when (section.palette) {
        SectionPalette.WAKING -> listOf(Forest, Color(0xFF047857))
        SectionPalette.MORNING -> listOf(Color(0xFF0F766E), Color(0xFF0D9488))
        SectionPalette.DUHA -> listOf(Color(0xFFB45309), Color(0xFFD97706))
        SectionPalette.EVENING -> listOf(Color(0xFF1E3A5F), Color(0xFF1E40AF))
        SectionPalette.SLEEP -> listOf(Color(0xFF2D1B69), Color(0xFF4C1D95))
        SectionPalette.TAHAJJUD -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
        SectionPalette.FRIDAY -> listOf(Color(0xFF854D0E), Color(0xFF92400E))
        SectionPalette.PRAYER -> listOf(ForestDark, Forest)
        SectionPalette.PLAIN -> listOf(Forest, ForestMid)
    }
    Surface(shape = RoundedCornerShape(14.dp), color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors))
                // ↓ vertical 4 → 3, horizontal 12 → 10
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    // ↓ size 26 → 22, cornerRadius 10 → 8
                    .size(22.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(section.icon.orEmpty(), fontSize = 11.sp)  // ↓ 13 → 11
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(section.title.orEmpty(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)  // ↓ 13 → 12
                section.subtitle?.let {
                    Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                }
            }
        }
    }
}

// ─── Azkar item card — RTL text (Start = right), Amiri font for dots/tashkeel ──
@Composable
private fun AzkarItemCard(
    section: AzkarSection,
    index: Int,
    item: AzkarItem,
    remaining: Int,
    onCount: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFFDF5),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, Color(0x1A065F46), RoundedCornerShape(24.dp))
            .testTag("azkar_card_${section.id}_$index"),
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFCF7), Color(0xFFF7FBF8))))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            // ── Align all children to Start = right side in RTL ──
            horizontalAlignment = Alignment.Start,
        ) {
            // ── Surah / title label — right-aligned, reduced height ──────────
            item.title?.let {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Sage.copy(alpha = 0.9f),
                ) {
                    Text(
                        text = it,
                        // ↓ vertical 5 → 3
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color = Forest,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                    )
                }
            }

            // ── Main azkar text — right-aligned, Amiri for all (fixes dots/tashkeel) ──
            Text(
                text = item.text,
                style = if (item.isQuran) {
                    TextStyle(
                        fontFamily = TajawalFamily,
                        fontSize = 21.sp,
                        // ↓ lineHeight increased slightly to prevent dot/tashkeel merging
                        lineHeight = 30.sp,
                        color = ForestDark,
                    )
                } else {
                    TextStyle(
                        // ↑ TajawalFamily for all Arabic text — fixes letter dots
                        //   merging with tashkeel (proper glyph spacing)
                        fontFamily = TajawalFamily,
                        fontSize = 17.sp,
                        // ↓ lineHeight generous to prevent dot/tashkeel overlap
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                    )
                },
                // ↓ TextAlign.Start = right side in RTL layout
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Count bubble (full width, unchanged internal design) ──────────
            CountBubble(remaining = remaining, total = item.count, onCount = onCount)

            // ── Fadl box — right-aligned, reduced padding ─────────────────────
            item.fadl?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF2D8),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "فضل: $it",
                        // ↓ padding 10 → 8
                        modifier = Modifier.padding(8.dp),
                        color = Color(0xFF8D5E10),
                        style = MaterialTheme.typography.bodySmall,
                        // ↓ right-aligned
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }
    }
}

// ─── Count bubble — reduced vertical padding ─────────────────────────────────
@Composable
private fun CountBubble(remaining: Int, total: Int, onCount: () -> Unit) {
    val progress = if (total <= 0) 1f else 1f - (remaining.toFloat() / total.toFloat())
    val complete = remaining == 0
    val shape = RoundedCornerShape(14.dp)

    Surface(
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = remaining > 0, onClick = onCount),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    if (complete) Brush.linearGradient(listOf(Color(0xFF0A7555), ForestDark))
                    else Brush.verticalGradient(listOf(Color(0xFFFCFFFD), Color(0xFFEAF7F1))),
                )
                .border(
                    width = 1.dp,
                    color = if (complete) Forest else Color(0x22065F46),
                    shape = shape,
                )
                // ↓ vertical 3 → 2
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),  // ↓ 3 → 2
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (complete) "اكتمل الذكر" else "اضغط للعد",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (complete) Color.White.copy(alpha = 0.86f) else ForestMid,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (complete) "تم" else remaining.toString(),
                    fontSize = 19.sp,   // ↓ 20 → 19
                    fontWeight = FontWeight.ExtraBold,
                    color = if (complete) Color.White else ForestDark,
                )
                if (total > 1) {
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "/$total",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (complete) Color.White.copy(alpha = 0.72f) else Color(0xFF7F8F89),
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)      // ↓ 4 → 3
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (complete) Color.White.copy(alpha = 0.18f) else Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)  // ↓ 4 → 3
                        .background(
                            if (complete) Brush.horizontalGradient(listOf(GoldBright, GoldLight))
                            else Brush.horizontalGradient(listOf(ForestLight, Forest)),
                        ),
                )
            }
        }
    }
}

// ─── Utility composables ──────────────────────────────────────────────────────
@Composable
private fun EmptyStateCard() {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color(0xFFD6D3D1), modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text("لا توجد عناصر لعرضها الآن", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(999.dp), color = ForestDark, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("وضع عدم الاتصال — التطبيق يعمل بالكامل", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ToastCard(message: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), shadowElevation = 12.dp, color = ForestDark) {
        Text(text = message, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

// ─── Utility ──────────────────────────────────────────────────────────────────
private fun formatCountdown(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private val AccountBalanceMosqueIcon = Icons.Rounded.AccountBalance
