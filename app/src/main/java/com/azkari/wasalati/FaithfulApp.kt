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
import androidx.compose.foundation.layout.defaultMinSize
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
                            colors = listOf(ForestLight.copy(alpha = 0.05f), Cream),
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

// ─── Brand mark: exact mosque SVG from index.html ────────────────────────────

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
                lineTo(11f * s, 11f * s)
                lineTo(9.5f * s, 11f * s)
                lineTo(8f * s, 13f * s)
                lineTo(4f * s, 13f * s)
                lineTo(4f * s, 27f * s)
                lineTo(28f * s, 27f * s)
                lineTo(28f * s, 13f * s)
                lineTo(24f * s, 13f * s)
                lineTo(22.5f * s, 11f * s)
                lineTo(21f * s, 11f * s)
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
                moveTo(14f * s, 27f * s)
                lineTo(14f * s, 22f * s)
                cubicTo(14f * s, 20.9f * s, 14.9f * s, 20f * s, 16f * s, 20f * s)
                cubicTo(17.1f * s, 20f * s, 18f * s, 20.9f * s, 18f * s, 22f * s)
                lineTo(18f * s, 27f * s)
                close()
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
                moveTo(17.5f * s, 5.5f * s)
                lineTo(17.8f * s, 6.4f * s)
                lineTo(18.7f * s, 6.4f * s)
                lineTo(18.0f * s, 7.0f * s)
                lineTo(18.3f * s, 7.9f * s)
                lineTo(17.5f * s, 7.3f * s)
                lineTo(16.7f * s, 7.9f * s)
                lineTo(17.0f * s, 7.0f * s)
                lineTo(16.3f * s, 6.4f * s)
                lineTo(17.2f * s, 6.4f * s)
                close()
            }
            drawPath(star, gold, style = Fill)
        }
    }
}

// ─── Header button — matches HTML hdr-btn: 30×30dp, 9dp radius, white bg ─────

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
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

// ─── City pill — matches HTML city-btn: 30dp tall, 9dp radius ────────────────

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
            Icon(
                Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = Color(0xFFF87171),
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151),
                maxLines = 1,
            )
        }
    }
}

// ─── Sticky header — matches HTML <header>: px-4, pt-2, pb-1.5, sticky ───────

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
    Surface(
        color = Color(0xFFFDFCFA).copy(alpha = 0.97f),
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // px-4 pt-2 pb-1.5 → 16/8/6dp — exact match to HTML header padding
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // ── Top row ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BrandMark()

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    // text-lg font-extrabold → 18sp ExtraBold gradient
                    Text(
                        text = "أذكاري وصلاتي",
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF065F46), Color(0xFF10B981)),
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                        ),
                        maxLines = 1,
                    )
                    // text-[10px] text-gray-400 → 10sp
                    Text(
                        text = uiState.hijriDate,
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                    )
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
                            Text(
                                uiState.qada.values.sum().toString(),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }

                CityButton(
                    label = uiState.currentCity?.name ?: "اختر مدينة",
                    onClick = onOpenCity,
                )
            }

            // mb-3 → 12dp gap after top row
            Spacer(Modifier.height(10.dp))

            // ── Prayer banner ─────────────────────────────────────────────────
            if (uiState.currentCity != null && uiState.prayerSummary != null) {
                PrayerBanner(
                    uiState = uiState,
                    onToggle = onToggleBanner,
                    onOpenTracker = onOpenTracker,
                )
                // mb-1.5 between banner and Quran card
                Spacer(Modifier.height(6.dp))
            }

            // ── Quran entry card — mt-1.5 mb-1.5 ─────────────────────────────
            QuranEntryCard(onOpenQuran = onOpenQuran)

            // ── Daily Quran tracker ───────────────────────────────────────────
            if (uiState.quran.dailyGoal > 0) {
                Spacer(Modifier.height(6.dp))
                QuranDailyTrackerCard(uiState.quran)
            }

            // py-0.5 tab strip
            Spacer(Modifier.height(8.dp))
            TabStrip(selected = uiState.selectedTab, onSelect = onSelectTab)
            Spacer(Modifier.height(2.dp))
        }
    }
}

// ─── Prayer banner — matches HTML #prayer-banner with exact collapsed/expanded ─

@Composable
private fun PrayerBanner(
    uiState: AppUiState2,
    onToggle: () -> Unit,
    onOpenTracker: () -> Unit,
) {
    val summary = uiState.prayerSummary ?: return

    // Collapsed: border-radius 3rem (48dp) — Expanded: 1.5rem (24dp)
    // HTML: border-radius: 1.5rem → 24dp; collapsed: 3rem → 48dp
    val collapsedRadius = 48.dp
    val expandedRadius  = 24.dp
    val radius = if (uiState.bannerCollapsed) collapsedRadius else expandedRadius
    val shape  = RoundedCornerShape(radius)

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
        Box(
            modifier = Modifier.background(
                // Linear gradient matching HTML: 145deg, #0a1628 0%, #052e16 55%, #064e3b 100%
                Brush.linearGradient(listOf(Color(0xFF0A1628), ForestDark, Color(0xFF064E3B))),
            ),
        ) {
            // Ambient glows — matching HTML pseudo-divs
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .background(Brush.radialGradient(listOf(Color(0x2610B981), Color.Transparent))),
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomStart)
                    .background(Brush.radialGradient(listOf(Color(0x1F3B82F6), Color.Transparent))),
            )

            Column {
                if (uiState.bannerCollapsed) {
                    // ── COLLAPSED STATE ───────────────────────────────────────
                    // Matches HTML compact bar in collapsed mode:
                    // padding: 8px 16px, min-height: 46px — just the row, no body
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 46.dp)
                            .clickable(onClick = onToggle)
                            // padding: 8px 16px — exact HTML match
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Countdown + next prayer name block
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            // text-[9px] text-emerald-400/70 — 9sp
                            Text(
                                text = "المتبقي للصلاة القادمة",
                                color = ForestLight.copy(alpha = 0.70f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                // text-xl font-mono font-bold — 20sp
                                Text(
                                    text = formatCountdown(summary.countdownMillis),
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        letterSpacing = 1.1.sp,
                                        color = Color.White,
                                    ),
                                )
                                Spacer(Modifier.width(5.dp))
                                Row(
                                    modifier = Modifier.padding(bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // text-[10px] text-slate-400 — arrow + name
                                    Text("←", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        summary.nextPrayer.displayName(),
                                        color = ForestLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        }

                        // Prayer dots
                        PrayerDotsRow(uiState.prayerDots)
                        Spacer(Modifier.width(8.dp))

                        // Chevron toggle icon
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowUp,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(11.dp),
                            )
                        }
                    }
                } else {
                    // ── EXPANDED STATE ────────────────────────────────────────

                    // Grabber — matches HTML .banner-grabber: 32×4dp, margin 0 auto 2px
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                        )
                    }

                    // Compact bar — always visible when expanded
                    // HTML: padding: 8px 16px, min-height: 46px
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 46.dp)
                            .clickable(onClick = onToggle)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "المتبقي للصلاة القادمة",
                                color = ForestLight.copy(alpha = 0.70f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                // text-xl → 20sp
                                Text(
                                    text = formatCountdown(summary.countdownMillis),
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        letterSpacing = 1.1.sp,
                                        color = Color.White,
                                    ),
                                )
                                Spacer(Modifier.width(5.dp))
                                Row(
                                    modifier = Modifier.padding(bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("←", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        summary.nextPrayer.displayName(),
                                        color = ForestLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        }

                        PrayerDotsRow(uiState.prayerDots)
                        Spacer(Modifier.width(6.dp))

                        // w-6 h-6 rounded-full bg-white/10 — 24×24dp
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowUp,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }

                // ── Expandable body — matches HTML #banner-body: p 0 14px 14px ──
                AnimatedVisibility(!uiState.bannerCollapsed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            // padding: 0 14px 14px → start/end 14dp, bottom 14dp
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Sunan box — matches HTML .sunan-box rounded-xl p-2.5 mb-3
                        SunnahStrip(summary.sunnahInfo)
                        // Prayer grid — matches HTML grid-cols-5 gap-1.5 mb-2.5
                        PrayerGrid(uiState.prayerDots)
                        // Footer row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "متابعة الصلوات",
                                color = ForestLight,
                                // text-[10px] → 10sp
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(onClick = onOpenTracker),
                            )
                            Spacer(Modifier.weight(1f))
                            // text-[10px] text-slate-500
                            Text(
                                text = "${uiState.todayDoneCount}/5 اليوم",
                                color = Color.White.copy(alpha = 0.38f),
                                fontSize = 10.sp,
                            )
                            if (uiState.streakDays >= 2) {
                                Spacer(Modifier.width(8.dp))
                                // streak-badge gradient pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFF97316))))
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "🔥 ${uiState.streakDays}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Prayer dots — matches HTML .prayer-dot: 9×9dp circle ───────────────────

@Composable
private fun PrayerDotsRow(dots: List<PrayerDotUi>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dots.forEach { dot ->
            val (bg, border) = when (dot.status) {
                PrayerDotStatus.DONE    -> ForestLight to ForestLight
                PrayerDotStatus.MISSED  -> Color(0xFFF43F5E) to Color(0xFFF43F5E)
                PrayerDotStatus.PENDING -> Color.White.copy(alpha = 0.13f) to Color.White.copy(alpha = 0.18f)
                PrayerDotStatus.UPCOMING -> Color.White.copy(alpha = 0.04f) to Color.White.copy(alpha = 0.07f)
            }
            // .prayer-dot: 9×9dp, border-radius 50%, border 1.5px
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(bg, androidx.compose.foundation.shape.CircleShape)
                    .border(1.5.dp, border, androidx.compose.foundation.shape.CircleShape),
            )
        }
    }
}

// ─── Sunnah strip — matches HTML .sunan-box: rounded-xl p-2.5 text-center ────

@Composable
private fun SunnahStrip(sunnahInfo: SunnahInfo) {
    // rounded-xl → 12dp, p-2.5 → 10dp padding
    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.09f), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // background: rgba(255,255,255,.05)
                .background(Color.White.copy(alpha = 0.05f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // text-[9px] text-slate-500
                Text("قبل", color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                // text-[11px] font-medium text-white
                Text(sunnahInfo.pre, color = Color.White, textAlign = TextAlign.Center, fontSize = 11.sp)
            }
            // h-4 w-px bg-white/15 — 1×16dp divider
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.15f)),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("بعد", color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                Text(sunnahInfo.post, color = Color.White, textAlign = TextAlign.Center, fontSize = 11.sp)
            }
        }
    }
}

// ─── Prayer grid — matches HTML grid-cols-5 gap-1.5 (6dp) ───────────────────

@Composable
private fun PrayerGrid(dots: List<PrayerDotUi>) {
    // gap-1.5 → 6dp
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        dots.forEach { item ->
            val alpha = if (item.status == PrayerDotStatus.UPCOMING) 1f else 0.55f
            // rounded-xl → 12dp, p-2 → 8dp
            Surface(
                modifier = Modifier.weight(1f).alpha(alpha),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.08f),
            ) {
                Column(
                    // p-2 → 8dp, text-center
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    // text-[9px] text-slate-400 — prayer name
                    Text(item.label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                    // text-sm font-semibold — prayer time
                    Text(item.timeLabel, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    // mt-1.5 dot
                    Spacer(Modifier.height(2.dp))
                    PrayerDotsRow(listOf(item))
                }
            }
        }
    }
}

// ─── Quran entry card — matches HTML Quran button: px-4 py-2.5 rounded-xl ────

@Composable
private fun QuranEntryCard(onOpenQuran: () -> Unit) {
    // rounded-xl → 12dp
    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quran_entry_card"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    // bg gradient: rgba(212,168,83,0.09) → transparent
                    Brush.linearGradient(listOf(GoldBright.copy(alpha = 0.09f), GoldBright.copy(alpha = 0.02f))),
                )
                // border: 1px solid rgba(212,168,83,0.25)
                .border(1.dp, GoldBright.copy(alpha = 0.25f), shape)
                .clickable(onClick = onOpenQuran)
                // px-4 py-2.5 → 16dp/10dp
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                // w-7 h-7 rounded-lg → 28×28dp, 8dp radius
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.80f))
                        .border(1.dp, Color(0x80FFEDD5), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📖", fontSize = 14.sp)
                }
                Spacer(Modifier.width(10.dp))
                // font-extrabold text-[14px] Amiri — exact HTML match
                Text(
                    text = "القرآن الكريم",
                    style = TextStyle(
                        fontFamily = AmiriFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = AmberText,
                    ),
                )
            }
            // قراءة واستماع — text-[9px] tracking-wide
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(0.70f),
            ) {
                Text(
                    text = "قراءة واستماع",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA38A64),
                )
                Icon(
                    Icons.Rounded.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color(0xFFA38A64),
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

// ─── Daily Quran tracker — matches HTML py-2.5 px-4 rounded-2xl ──────────────

@Composable
private fun QuranDailyTrackerCard(quranUiState: QuranUiState) {
    val goal     = quranUiState.dailyGoal.coerceAtLeast(1)
    val progress = (quranUiState.dailyLog.pagesRead.size.toFloat() / goal).coerceIn(0f, 1f)
    // rounded-2xl → 16dp
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
                // py-2.5 px-4 → 10dp/16dp — exact HTML match
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // fas fa-check-circle → 10dp icon
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = GoldBright.copy(alpha = 0.50f),
                modifier = Modifier.size(10.dp),
            )
            Spacer(Modifier.width(8.dp))
            // "وِردك اليومي" text-xs font-medium
            Text(
                "وِردك اليومي",
                modifier = Modifier.weight(1f),
                color = Color(0xFF8B7355),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            // progress fraction — font-bold
            Text(
                text = "${quranUiState.dailyLog.pagesRead.size}/${quranUiState.dailyGoal}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB48530),
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(10.dp))
            // w-24 h-1.5 → 96×6dp progress bar
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(GoldBright.copy(alpha = 0.15f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(GoldBright, Color(0xFFB48530)))),
                )
            }
            if (quranUiState.stats.streakDays >= 2) {
                Spacer(Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 10.sp)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        quranUiState.stats.streakDays.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB48530),
                    )
                }
            }
        }
    }
}

// ─── Tab strip — matches HTML nav: gap-1.5 (6dp), py-0.5 ────────────────────

@Composable
private fun TabStrip(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .testTag("tab_strip"),
        // gap-1.5 → 6dp
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
    // rounded-2xl → 20dp radius (HTML uses rounded-2xl)
    val shape = RoundedCornerShape(20.dp)
    val background = when {
        selected -> Forest
        isFriday -> Color(0xFFFFFBEB).copy(alpha = 0.70f)
        else     -> Color.White.copy(alpha = 0.70f)
    }
    val border = when {
        selected -> Color.Transparent
        isFriday -> Color(0xFFFEF3C7).copy(alpha = 0.80f)
        else     -> Color(0xFFF3F4F6).copy(alpha = 0.80f)
    }
    val labelColor = when {
        selected -> Color.White
        isFriday -> Color(0xFFB45309)
        else     -> Color(0xFF6B7280)
    }
    Surface(
        shape = shape,
        color = background,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier
            .border(1.dp, border, shape)
            .clickable(onClick = onClick),
    ) {
        // px-3.5 py-1.5 → 14dp/6dp; text-xs font-bold → 12sp
        Text(
            tab.chipLabel,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 12.sp,
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
                    delta > 18         -> viewModel.setBannerCollapsed(true)
                }
                if (delta > 2 || delta < -2) previousScroll = currentScroll
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_list"),
        state = listState,
        // px-4 pt-2 pb-5 → 16/16/8/120dp (120dp accounts for nav bar)
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        // space-y-3 → 12dp gap
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (uiState.currentCity == null || uiState.currentPrayerTimes == null || uiState.prayerSummary == null) {
                EmptyCityCard(onOpenCity = { viewModel.openModal(AppModal.CITY) })
            }
        }

        uiState.suggestion?.let { suggestion ->
            item {
                FaithfulSuggestionCard(
                    suggestion = suggestion,
                    onReset = viewModel::resetAutoTab,
                )
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
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    // mt-8 pb-2 → 32dp top, 8dp bottom
                    .padding(top = 32.dp, bottom = 8.dp),
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
            Text(
                "اختر مدينتك لعرض مواقيت الصلاة والأذكار المناسبة.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onOpenCity) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("اختيار مدينة", fontSize = 13.sp)
            }
        }
    }
}

// ─── Suggestion card — matches HTML auto-suggestion-msg: px-4 py-1 rounded-xl ─

@Composable
private fun FaithfulSuggestionCard(suggestion: String, onReset: () -> Unit) {
    // rounded-xl → 12dp
    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD1FAE5), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFECFDF5).copy(alpha = 0.80f))
                // px-4 py-1.0 → 16dp/4dp
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⏱️", fontSize = 12.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                suggestion,
                modifier = Modifier.weight(1f),
                color = Color(0xFF065F46),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(9.dp))
                    .clickable(onClick = onReset),
            ) {
                Text(
                    "تلقائي",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestDark,
                )
            }
        }
    }
}

// ─── Friday Kahf card — matches HTML friday-kahf-banner: px-4 py-3 rounded-2xl

@Composable
private fun FridayKahfCard(onOpen: () -> Unit) {
    // rounded-2xl → 16dp
    val shape = RoundedCornerShape(20.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x38FBBF24), shape),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFF422006), Color(0xFF78350F), Color(0xFF92400E))))
                // px-4 py-3 → 16dp/12dp
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // w-9 h-9 rounded-xl → 36×36dp, 12dp radius
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FBBF24)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🕌", fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // text-xs font-extrabold Amiri — color #fbbf24
                Text(
                    "يوم الجمعة المبارك",
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(fontFamily = AmiriFamily),
                    fontSize = 12.sp,
                )
                // text-[10px] leading-relaxed — quote text
                Text(
                    "«من قرأ سورة الكهف يوم الجمعة أضاء له النور ما بين الجمعتين»",
                    color = Color(0xBFFDE68A),
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                )
            }
            Spacer(Modifier.width(8.dp))
            // Button — px-3 py-2 rounded-xl
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x2EFBBF24),
                modifier = Modifier
                    .border(1.dp, Color(0x4DFBBF24), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpen),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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

// ─── Section divider — matches HTML section headers: gradient, rounded-2xl ───

@Composable
private fun SectionDivider(section: AzkarSection) {
    val colors = when (section.palette) {
        SectionPalette.WAKING   -> listOf(Forest, Color(0xFF047857))
        SectionPalette.MORNING  -> listOf(Color(0xFF0F766E), Color(0xFF0D9488))
        SectionPalette.DUHA     -> listOf(Color(0xFFB45309), Color(0xFFD97706))
        SectionPalette.EVENING  -> listOf(Color(0xFF1E3A5F), Color(0xFF1E40AF))
        SectionPalette.SLEEP    -> listOf(Color(0xFF2D1B69), Color(0xFF4C1D95))
        SectionPalette.TAHAJJUD -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
        SectionPalette.FRIDAY   -> listOf(Color(0xFF854D0E), Color(0xFF92400E))
        SectionPalette.PRAYER   -> listOf(ForestDark, Forest)
        SectionPalette.PLAIN    -> listOf(Forest, ForestMid)
    }
    // rounded-2xl → 16dp
    Surface(shape = RoundedCornerShape(16.dp), color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors))
                // px-3 py-2 → 12dp/8dp — compact, matches HTML section headers
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon box — 26×26dp, rounded-10dp
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(section.icon.orEmpty(), fontSize = 13.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                // text-sm font-extrabold text-white — 13sp
                Text(section.title.orEmpty(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                section.subtitle?.let {
                    // text-[9px] text-white/70
                    Text(it, color = Color.White.copy(alpha = 0.70f), fontSize = 9.sp)
                }
            }
        }
    }
}

// ─── Azkar item card — matches HTML .azkar-card: white bg, subtle border ─────

@Composable
private fun AzkarItemCard(
    section: AzkarSection,
    index: Int,
    item: AzkarItem,
    remaining: Int,
    onCount: () -> Unit,
) {
    // rounded-2xl → 16dp (HTML rounded-2xl = 16px)
    val cardShape = RoundedCornerShape(20.dp)
    Surface(
        shape = cardShape,
        // bg-white/warm gradient matching HTML card background
        color = Color(0xFFFFFDF5),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, Color(0x1A065F46), cardShape)
            .testTag("azkar_card_${section.id}_$index"),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFCF7), Color(0xFFF7FBF8))))
                // p-4 → 16dp; HTML uses px-4 py-4 equivalent
                .padding(horizontal = 16.dp, vertical = 14.dp),
            // gap equivalent between inner elements
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Optional title badge
            item.title?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Sage.copy(alpha = 0.90f),
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color = Forest,
                        fontWeight = FontWeight.Bold,
                        // text-[11px] → 11sp
                        fontSize = 11.sp,
                    )
                }
            }

            // Main azkar text — THE KEY CHANGE:
            // Quran text: Amiri font, 1.2rem (≈19sp), line-height 2.6 (≈49sp), emerald color
            // Non-quran: Tajawal bold, 17sp, line-height 34sp (≈2.0)
            Text(
                text = item.text,
                style = if (item.isQuran) {
                    TextStyle(
                        fontFamily = AmiriFamily,
                        // font-size: 1.2rem → ~19sp; line-height: 2.6 → 2.6*19 ≈ 50sp
                        fontSize = 19.sp,
                        lineHeight = 50.sp,
                        color = ForestDark,          // color: var(--emerald) = #065f46
                    )
                } else {
                    TextStyle(
                        color = Color(0xFF111827),   // near-black, Tailwind gray-900
                        fontSize = 17.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                textAlign = TextAlign.Center,
            )

            // Count button
            CountBubble(remaining = remaining, total = item.count, onCount = onCount)

            // Fadl box — matches HTML .fadl-box: bg #fef3c7, border #fde68a, 12px radius
            item.fadl?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    // background: linear-gradient(135deg, #fef3c7, #fffbeb) → approximate
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp)),
                ) {
                    Row(
                        // p: 8px 12px → 8dp/12dp
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // ⭐ icon in gold
                        Text("⭐", fontSize = 12.sp)
                        Text(
                            text = it,
                            // font-size: 12px → 12sp; color: #92400e
                            color = Color(0xFF92400E),
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
        }
    }
}

// ─── Count bubble — matches HTML count button: gradient, progress bar ─────────

@Composable
private fun CountBubble(remaining: Int, total: Int, onCount: () -> Unit) {
    val progress  = if (total <= 0) 1f else 1f - (remaining.toFloat() / total.toFloat())
    val complete  = remaining == 0
    val shape     = RoundedCornerShape(16.dp)
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
                // py: 4dp compact
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // "اضغط للعد" / "اكتمل الذكر" — text-[9px] font-extrabold
            Text(
                text = if (complete) "اكتمل الذكر" else "اضغط للعد",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (complete) Color.White.copy(alpha = 0.86f) else ForestMid,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                // Main number — text-xl font-extrabold → 20sp
                Text(
                    text = if (complete) "تم" else remaining.toString(),
                    fontSize = 20.sp,
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
            // Progress bar — h-1.5 (6dp) rounded-full
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (complete) Color.White.copy(alpha = 0.18f) else Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(
                            if (complete) Brush.horizontalGradient(listOf(GoldBright, GoldLight))
                            else Brush.horizontalGradient(listOf(ForestLight, Forest)),
                        ),
                )
            }
        }
    }
}

// ─── Empty / offline / toast ──────────────────────────────────────────────────

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
    // Matches HTML #offline-banner: rounded-full (pill), dark bg
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = ForestDark,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "وضع عدم الاتصال — التطبيق يعمل بالكامل",
                color = Color.White,
                // text-[11px] font-semibold → 11sp
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ToastCard(message: String, modifier: Modifier = Modifier) {
    // Matches HTML #toast: bg #1e293b, rounded-full, text-sm
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 12.dp,
        color = Color(0xFF1E293B),
    ) {
        Text(
            text = message,
            color = Color.White,
            // px-[18px] py-[9px] text-xs font-semibold → 9dp/18dp, 12sp
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─── Utility ──────────────────────────────────────────────────────────────────

private fun formatCountdown(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val hours   = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

// ─── Reference: mosque icon via AccountBalance (kept for Qada badge) ──────────
private val AccountBalanceMosqueIcon = Icons.Rounded.AccountBalance
