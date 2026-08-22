package com.systentando.nfc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.systentando.nfc.ui.screens.AdvancedCommandsScreen
import com.systentando.nfc.ui.screens.HomeScreen
import com.systentando.nfc.ui.screens.OnboardingScreen
import com.systentando.nfc.ui.screens.SettingsScreen
import com.systentando.nfc.ui.screens.SplashScreen
import com.systentando.nfc.ui.screens.TagDetailScreen
import com.systentando.nfc.ui.screens.WriteTagScreen
import com.systentando.nfc.ui.viewmodels.NFCViewModel
import com.systentando.nfc.util.openWhatsApp
import com.systentando.nfc.ui.viewmodels.SplashViewModel
import com.systentando.nfc.ui.viewmodels.WriteTagViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object WriteTag : Screen("write_tag")
    object TagDetail : Screen("tag_detail")
    object Settings : Screen("settings")
    object Advanced : Screen("advanced")
}

@Composable
fun AppNavigation(
    nfcViewModel: NFCViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    val state by nfcViewModel.uiState.collectAsState()
    val splashViewModel: SplashViewModel = hiltViewModel()
    val splashState by splashViewModel.uiState.collectAsState()
    val context = LocalContext.current
    splashViewModel.setPendingTag(state.pendingTag)

    LaunchedEffect(state.lastTag) {
        val tag = state.lastTag ?: return@LaunchedEffect
        val phoneRecord = tag.records
            .filterIsInstance<com.systentando.nfc.domain.model.NDEFRecord.Uri>()
            .firstOrNull { it.uri.startsWith("tel:") }
        phoneRecord?.let { openWhatsApp(context, it.uri.removePrefix("tel:")) }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onboardingSeen = splashState.onboardingSeen,
                hasPendingTag = state.pendingTag != null,
                onGoToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onGoToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = nfcViewModel,
                onWriteClick = { navController.navigate(Screen.WriteTag.route) },
                onAdvancedClick = { navController.navigate(Screen.Advanced.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onTagClick = { navController.navigate(Screen.TagDetail.route) }
            )
        }

        composable(Screen.TagDetail.route) {
            TagDetailScreen(
                tag = state.pendingTag ?: state.lastTag,
                viewModel = nfcViewModel,
                onBackClick = { navController.popBackStack() },
                onDeleteTag = { navController.popBackStack() }
            )
        }
        composable(Screen.WriteTag.route) {
            val writeViewModel: WriteTagViewModel = hiltViewModel()
            WriteTagScreen(
                viewModel = writeViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Advanced.route) {
            AdvancedCommandsScreen(
                viewModel = nfcViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
