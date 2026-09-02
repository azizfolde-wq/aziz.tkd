package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.AppDestination
import com.example.ui.screens.calculator.CalculatorScreen
import com.example.ui.screens.calculator.CalculatorViewModel
import com.example.ui.screens.clock.ClockScreen
import com.example.ui.screens.clock.ClockViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.weather.WeatherScreen
import com.example.ui.screens.weather.WeatherViewModel
import com.example.ui.theme.KurdishUtilityTheme

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel = viewModel(),
    clockViewModel: ClockViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    calculatorViewModel: CalculatorViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }

    KurdishUtilityTheme(themeMode = settingsState.themeMode) {
        // Enforce RTL Layout Direction for Kurdish (Sorani)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                bottomBar = {
                    AppBottomNavigationBar(
                        currentDestination = currentDestination,
                        onNavigateToDestination = { currentDestination = it }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(targetState = currentDestination, label = "ScreenTransition") { destination ->
                        when (destination) {
                            AppDestination.HOME -> HomeScreen(
                                viewModel = homeViewModel,
                                onNavigate = { currentDestination = it }
                            )
                            AppDestination.CLOCK -> ClockScreen(
                                viewModel = clockViewModel
                            )
                            AppDestination.WEATHER -> WeatherScreen(
                                viewModel = weatherViewModel
                            )
                            AppDestination.CALCULATOR -> CalculatorScreen(
                                viewModel = calculatorViewModel
                            )
                            AppDestination.SETTINGS -> SettingsScreen(
                                viewModel = settingsViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
