package com.azkari.wasalati

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val calculationMethods = listOf(
    "Egyptian",
    "UmmAlQura",
    "Kuwait",
    "Qatar",
    "Dubai",
    "MuslimWorldLeague",
    "NorthAmerica",
    "Singapore",
    "Turkey",
    "Tehran",
    "Karachi",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaithfulModalHost(
    uiState: AppUiState2,
    viewModel: FaithfulMainViewModel,
    notificationPermissionGranted: Boolean,
    exactAlarmPermissionGranted: Boolean,
    onRequestNotifications: () -> Unit,
    onRequestExactAlarms: () -> Unit,
    onUseCurrentLocation: () -> Unit,
) {
    when (uiState.activeModal) {
        AppModal.CITY -> ModalBottomSheet(onDismissRequest = viewModel::dismissModal) {
            CitySheet(
                cities = uiState.cityResults,
                onDismiss = viewModel::dismissModal,
                onSearch = viewModel::searchCities,
                onSelect = viewModel::setCity,
                onUseCurrentLocation = {
                    viewModel.dismissModal()
                    onUseCurrentLocation()
                },
            )
        }

        AppModal.SETTINGS -> ModalBottomSheet(onDismissRequest = viewModel::dismissModal) {
            SettingsSheet(
                uiState = uiState,
                notificationPermissionGranted = notificationPermissionGranted,
                exactAlarmPermissionGranted = exactAlarmPermissionGranted,
                onDismiss = viewModel::dismissModal,
                onOpenCity = { viewModel.openModal(AppModal.CITY) },
                onUseCurrentLocation = {
                    viewModel.dismissModal()
                    onUseCurrentLocation()
                },
                onChangeMethod = viewModel::changeCalcMethod,
                onUpdateReminders = viewModel::updateReminderSettings,
                onResetAuto = viewModel::resetAutoTab,
                onRequestNotifications = onRequestNotifications,
                onRequestExactAlarms = onRequestExactAlarms,
            )
        }

        AppModal.TRACKER -> ModalBottomSheet(onDismissRequest = viewModel::dismissModal) {
            TrackerSheet(
                uiState = uiState,
                onDismiss = viewModel::dismissModal,
                onMarkPrayer = viewModel::markPrayer,
            )
        }

        AppModal.QADA -> ModalBottomSheet(onDismissRequest = viewModel::dismissModal) {
            QadaSheet(
                qada = uiState.qada,
                onDismiss = viewModel::dismissModal,
                onChange = viewModel::changeQada,
            )
        }

        AppModal.PRAYER_CONFIRM -> PrayerPromptDialog(
            prompt = uiState.prayerPrompt,
            onDismiss = viewModel::dismissModal,
            onConfirm = viewModel::confirmPrayer,
            onSnooze = viewModel::snoozePrayerPrompt,
            onOpenQuran = { page -> viewModel.openQuranAtPage(page) },
        )

        AppModal.QURAN -> FaithfulQuranDialog(uiState = uiState, viewModel = viewModel)
        AppModal.NONE -> Unit
    }
}

@Composable
private fun CitySheet(
    cities: List<City>,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    onSelect: (City) -> Unit,
    onUseCurrentLocation: () -> Unit,
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("اختر مدينة", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onSearch(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ابحث باسم المدينة") },
            singleLine = true,
        )
        OutlinedButton(onClick = onUseCurrentLocation) {
            Icon(Icons.Rounded.MyLocation, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("استخدام موقعي الحالي")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 440.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(cities) { city ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(city) },
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(city.name, fontWeight = FontWeight.Bold)
                        Text(city.country, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text("إغلاق")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsSheet(
    uiState: AppUiState2,
    notificationPermissionGranted: Boolean,
    exactAlarmPermissionGranted: Boolean,
    onDismiss: () -> Unit,
    onOpenCity: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onChangeMethod: (String) -> Unit,
    onUpdateReminders: (ReminderSettings) -> Unit,
    onResetAuto: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestExactAlarms: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("الإعدادات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        }

        item {
            SettingsCard(title = "الموقع والمواقيت") {
                Text(
                    text = uiState.currentCity?.let { "${it.name} • ${it.country}" } ?: "لم يتم اختيار مدينة بعد",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpenCity) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("اختيار مدينة")
                    }
                    OutlinedButton(onClick = onUseCurrentLocation) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("موقعي الحالي")
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    calculationMethods.forEach { method ->
                        FilterChip(
                            selected = uiState.calcMethod == method,
                            onClick = { onChangeMethod(method) },
                            label = { Text(method) },
                        )
                    }
                }
                OutlinedButton(onClick = onResetAuto) {
                    Text("العودة للاقتراح التلقائي")
                }
            }
        }

        item {
            PermissionCard(
                title = "إذن الإشعارات",
                description = if (notificationPermissionGranted) "الإشعارات مفعلة." else "فعّل الإشعارات لتصلك تنبيهات الصلاة والأذكار.",
                isEnabled = notificationPermissionGranted,
                icon = Icons.Rounded.Notifications,
                accent = Forest,
                buttonLabel = "فتح الإذن",
                onClick = onRequestNotifications,
            )
        }

        item {
            PermissionCard(
                title = "المنبهات الدقيقة",
                description = if (exactAlarmPermissionGranted) "سيتم ضبط التذكيرات بدقة أكبر." else "افتح هذا الخيار لعمل التنبيهات الدقيقة بالخلفية.",
                isEnabled = exactAlarmPermissionGranted,
                icon = Icons.Rounded.Schedule,
                accent = Gold,
                buttonLabel = "فتح الإعداد",
                onClick = onRequestExactAlarms,
            )
        }

        item {
            SettingsCard(title = "التنبيهات") {
                ReminderToggle("تذكير الصلوات", uiState.reminderSettings.prayerNotificationsEnabled) {
                    onUpdateReminders(uiState.reminderSettings.copy(prayerNotificationsEnabled = it))
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 5, 10, 15, 20, 30).forEach { offset ->
                        FilterChip(
                            selected = uiState.reminderSettings.prayerReminderOffsetMinutes == offset,
                            onClick = {
                                onUpdateReminders(
                                    uiState.reminderSettings.copy(prayerReminderOffsetMinutes = offset),
                                )
                            },
                            label = { Text(if (offset == 0) "عند الأذان" else "قبل $offset د") },
                        )
                    }
                }
                ReminderToggle("أذكار الصباح", uiState.reminderSettings.morningAzkarEnabled) {
                    onUpdateReminders(uiState.reminderSettings.copy(morningAzkarEnabled = it))
                }
                ReminderToggle("أذكار المساء", uiState.reminderSettings.eveningAzkarEnabled) {
                    onUpdateReminders(uiState.reminderSettings.copy(eveningAzkarEnabled = it))
                }
                ReminderToggle("أذكار النوم", uiState.reminderSettings.sleepAzkarEnabled) {
                    onUpdateReminders(uiState.reminderSettings.copy(sleepAzkarEnabled = it))
                }
                ReminderToggle("تذكير الجمعة", uiState.reminderSettings.fridayKahfEnabled) {
                    onUpdateReminders(uiState.reminderSettings.copy(fridayKahfEnabled = it))
                }
            }
        }

        item {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("إغلاق")
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                content()
            },
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    SettingsCard(title = title) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent)
            Spacer(Modifier.width(8.dp))
            Text(if (isEnabled) "مفعّل" else "غير مفعّل", color = accent, fontWeight = FontWeight.Bold)
        }
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = onClick) { Text(buttonLabel) }
    }
}

@Composable
private fun ReminderToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackerSheet(
    uiState: AppUiState2,
    onDismiss: () -> Unit,
    onMarkPrayer: (PrayerKey, String?) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("متابعة الصلوات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryPill("السلسلة", "${uiState.streakDays} يوم", modifier = Modifier.weight(1f))
                SummaryPill("القضاء", uiState.qada.values.sum().toString(), modifier = Modifier.weight(1f), accent = Warning)
            }
        }
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = ForestDark) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    uiState.weeklyPrayerUi.reversed().forEach { stat ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        when {
                                            stat.done == 5 -> ForestLight
                                            stat.done >= 3 -> Color(0xFF6EE7B7)
                                            stat.done >= 1 -> Color(0xFFFCD34D)
                                            stat.missed > 0 -> Color(0xFFFDA4AF)
                                            else -> Color.White.copy(alpha = 0.12f)
                                        },
                                    ),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("${stat.done}/5", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        items(PrayerKey.entries.toList()) { prayerKey ->
            val status = uiState.prayerLog[prayerKey]
            val prayerTime = uiState.currentPrayerTimes?.get(prayerKey)
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(prayerKey.displayIcon(), fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prayerKey.displayName(), fontWeight = FontWeight.ExtraBold)
                            Text(
                                prayerTime?.let { java.text.SimpleDateFormat("hh:mm a", Locale("ar")).format(it) } ?: "--:--",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        PrayerStatusPill(status = status)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = status == "done", onClick = { onMarkPrayer(prayerKey, "done") }, label = { Text("صليت") }, leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) })
                        FilterChip(selected = status == "missed", onClick = { onMarkPrayer(prayerKey, "missed") }, label = { Text("فاتتني") }, leadingIcon = { Icon(Icons.Rounded.Close, contentDescription = null) })
                        FilterChip(selected = status == null, onClick = { onMarkPrayer(prayerKey, null) }, label = { Text("مسح") })
                    }
                }
            }
        }
        items(uiState.weeklyPrayerUi) { stat ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stat.label, modifier = Modifier.width(56.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE7E5E4)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stat.done / 5f)
                            .height(8.dp)
                            .background(if (stat.done == 5) Forest else Gold),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("${stat.done}/5", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("إغلاق") }
        }
    }
}

@Composable
private fun QadaSheet(
    qada: Map<PrayerKey, Int>,
    onDismiss: () -> Unit,
    onChange: (PrayerKey, Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("سجل القضاء", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        }
        items(PrayerKey.entries.toList()) { prayerKey ->
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prayerKey.displayName(), fontWeight = FontWeight.ExtraBold)
                        Text("عدد الصلوات المقضية", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onChange(prayerKey, -1) }) {
                        Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = null)
                    }
                    Text((qada[prayerKey] ?: 0).toString(), fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    IconButton(onClick = { onChange(prayerKey, 1) }) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Forest)
                    }
                }
            }
        }
        item {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("إغلاق") }
        }
    }
}

@Composable
private fun PrayerPromptDialog(
    prompt: PrayerPromptUi?,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit,
    onSnooze: () -> Unit,
    onOpenQuran: (Int) -> Unit,
) {
    if (prompt == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onConfirm(false) }) { Text("فاتتني") }
                TextButton(onClick = { onConfirm(true) }) { Text("صليت") }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                prompt.quranTargetPage?.let { page ->
                    TextButton(onClick = { onOpenQuran(page) }) {
                        Text("افتح القرآن")
                    }
                }
                TextButton(onClick = onSnooze) { Text("ذكرني لاحقًا") }
            }
        },
        title = { Text("هل أديت صلاة ${prompt.prayerName}؟") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("وقتها كان ${prompt.timeLabel}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("يمكنك تثبيت الإجابة الآن أو فتح القرآن للوِرد التالي.", style = MaterialTheme.typography.bodySmall)
            }
        },
    )
}

@Composable
private fun SummaryPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = Forest,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = 0.12f)) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(title, color = accent, style = MaterialTheme.typography.labelMedium)
            Text(value, color = accent, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun PrayerStatusPill(status: String?) {
    val (label, accent) = when (status) {
        "done" -> "مؤداة" to Forest
        "missed" -> "فائتة" to Warning
        else -> "قيد المتابعة" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = accent,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
