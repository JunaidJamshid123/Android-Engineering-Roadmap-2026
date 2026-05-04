package com.example.practiceapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nexusbank.feature.auth.ui.LoginScreen
import com.example.nexusbank.feature.dashboard.ui.DashboardScreen
import com.example.nexusbank.feature.dashboard.ui.MoreOptionsScreen
import com.example.nexusbank.feature.onboarding.ui.SplashScreen

@Composable
fun NexusBankNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onSendMoneyClick = { },
                onPaymentClick = { },
                onCardsClick = { },
                onMoreClick = {
                    navController.navigate(Screen.MoreOptions.route)
                },
                onAccountClick = { },
                onDrawerProfileClick = { },
                onDrawerStatementsClick = { },
                onDrawerTransactionsClick = { },
                onDrawerBeneficiariesClick = { },
                onDrawerVerificationClick = { },
                onDrawerNotificationsClick = { },
                onDrawerSecurityClick = { },
                onDrawerHelpClick = { },
                onDrawerAboutClick = { }
            )
        }

        composable(Screen.MoreOptions.route) {
            MoreOptionsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onOptionClick = { /* TODO: navigate to individual screens */ }
            )
        }
    }
}
