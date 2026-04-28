package com.example.practiceapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nexusbank.feature.accounts.ui.AccountsScreen
import com.example.nexusbank.feature.auth.ui.LoginScreen
import com.example.nexusbank.feature.cards.ui.CardsScreen
import com.example.nexusbank.feature.dashboard.ui.DashboardScreen
import com.example.nexusbank.feature.investments.ui.InvestmentsScreen
import com.example.nexusbank.feature.kyc.ui.KycScreen
import com.example.nexusbank.feature.loans.ui.LoansScreen
import com.example.nexusbank.feature.notifications.ui.NotificationsScreen
import com.example.nexusbank.feature.onboarding.ui.SplashScreen
import com.example.nexusbank.feature.profile.ui.ProfileScreen
import com.example.nexusbank.feature.transfers.ui.TransactionsScreen

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
                onMenuClick = { },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSendMoneyClick = { },
                onPaymentClick = { },
                onCardsClick = { navController.navigate(Screen.Cards.route) },
                onMoreClick = { },
                onAccountClick = { accountId ->
                    navController.navigate(Screen.Transactions.createRoute(accountId))
                }
            )
        }

        composable(Screen.Accounts.route) {
            AccountsScreen(
                onBackClick = { navController.popBackStack() },
                onAccountClick = { accountId ->
                    navController.navigate(Screen.Transactions.createRoute(accountId))
                }
            )
        }

        composable(Screen.Cards.route) {
            CardsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.Transactions.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) {
            TransactionsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Loans.route) {
            LoansScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Kyc.route) {
            KycScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Investments.route) {
            InvestmentsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
