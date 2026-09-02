package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.AppThemeMode
import com.example.data.repository.TempUnit
import com.example.ui.components.KurdistanFlag
import com.example.ui.theme.KurdishGold
import com.example.ui.theme.KurdishSun

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.messageSnackbar) {
        state.messageSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("settings_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header with Kurdistan Flag on Top-Left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ڕێکخستنەکان",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                KurdistanFlag(
                    width = 36.dp,
                    height = 24.dp,
                    cornerRadius = 6.dp,
                    elevation = 3.dp,
                    modifier = Modifier.testTag("settings_top_kurdistan_flag")
                )
            }

            // Section 1: Clock & Time Settings
            SettingsSectionHeader(title = "کاتژمێر و کات", icon = Icons.Outlined.AccessTime)
            SettingsCard {
                // 12-hour vs 24-hour
                SettingsSwitchRow(
                    title = "سیستەمی ٢٤ کاتژمێری",
                    subtitle = if (state.is24Hour) "کات بەشێوازی 11:00:17 پیشاندەدرێت" else "کات بەشێوازی 11:00:17 ب.ن پیشاندەدرێت",
                    checked = state.is24Hour,
                    onCheckedChange = { viewModel.set24Hour(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Military Time (1200:33)
                SettingsSwitchRow(
                    title = "کاتی سەربازی (Military Time: 1200:33)",
                    subtitle = if (state.isMilitaryTime) "کات بەشێوازی سەربازی پیشاندەدرێت (نموونە: 1200:33)" else "کات بەشێوازی ئاسایی کاتژمێر پیشاندەدرێت",
                    checked = state.isMilitaryTime,
                    onCheckedChange = { viewModel.setMilitaryTime(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Kurdish Digits
                SettingsSwitchRow(
                    title = "ژمارەی کوردی / ڕۆژهەڵاتی (٠-٩)",
                    subtitle = if (state.useKurdishDigits) "ژمارەکان بە کوردی دەنووسرێن" else "ژمارەکان بە ئینگلیزی دەنووسرێن",
                    checked = state.useKurdishDigits,
                    onCheckedChange = { viewModel.setUseKurdishDigits(it) }
                )
            }

            // Section 2: Appearance & Theme
            SettingsSectionHeader(title = "ڕووکار و شێواز", icon = Icons.Outlined.DarkMode)
            SettingsCard {
                Text(
                    text = "دۆخی ڕووکار (Theme)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        title = "سیستەم",
                        isSelected = state.themeMode == AppThemeMode.SYSTEM,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setThemeMode(AppThemeMode.SYSTEM) }

                    ThemeOptionButton(
                        title = "ڕۆشن",
                        isSelected = state.themeMode == AppThemeMode.LIGHT,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setThemeMode(AppThemeMode.LIGHT) }

                    ThemeOptionButton(
                        title = "تاریک",
                        isSelected = state.themeMode == AppThemeMode.DARK,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setThemeMode(AppThemeMode.DARK) }
                }
            }

            // Section 3: Weather Preferences
            SettingsSectionHeader(title = "کەشوهەوا", icon = Icons.Outlined.WbSunny)
            SettingsCard {
                Text(
                    text = "یەکەی پلەی گەرمی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        title = "سیلیزی (°C)",
                        isSelected = state.tempUnit == TempUnit.CELSIUS,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setTempUnit(TempUnit.CELSIUS) }

                    ThemeOptionButton(
                        title = "فەهرەنهایت (°F)",
                        isSelected = state.tempUnit == TempUnit.FAHRENHEIT,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setTempUnit(TempUnit.FAHRENHEIT) }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Clear Weather Cache
                SettingsActionRow(
                    title = "سڕینەوەی داتای کەشوهەوا",
                    subtitle = "سڕینەوەی داتای پاشەکەوتکراوی شارەکان لە میمۆری",
                    icon = Icons.Outlined.DeleteOutline,
                    onClick = { showClearCacheDialog = true }
                )
            }

            // Section 4: Calculator Preferences
            SettingsSectionHeader(title = "ژمێرەر", icon = Icons.Outlined.Calculate)
            SettingsCard {
                SettingsActionRow(
                    title = "سڕینەوەی مێژووی ژماردن",
                    subtitle = "تەواوی (${state.historyCount}) هاوکێشەی تۆمارکراو بسڕەوە",
                    icon = Icons.Outlined.Delete,
                    onClick = { showClearHistoryDialog = true }
                )
            }

            // Section 5: Language & Info
            SettingsSectionHeader(title = "زمان و دەربارە", icon = Icons.Outlined.Info)
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "زمانی بەرنامە",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "کوردی سۆرانی (RTL)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "کوردی",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // About app
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "ئامرازی کورد (Kurdish Utility)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "وەشان: 1.0.0",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "دروستکراوە لەلایەن: Aziz khder",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KurdishGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Dialogs
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("سڕینەوەی مێژووی ژمێرەر") },
            text = { Text("ئایا دڵنیایت لە سڕینەوەی هەموو هاوکێشە تۆمارکراوەکان؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCalculatorHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("بەڵێ، بسڕەوە", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("پاشگەزبوونەوە")
                }
            }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("سڕینەوەی کەشی پاشەکەوتکراو") },
            text = { Text("ئایا دڵنیایت لە سڕینەوەی زانیاری کەشوهەوای پاشەکەوتکراو؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearWeatherCache()
                        showClearCacheDialog = false
                    }
                ) {
                    Text("بسڕەوە", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("پاشگەزبوونەوە")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun ThemeOptionButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
