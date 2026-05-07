package com.example.practiceapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nexusbank.feature.auth.ui.LoginScreen
import com.example.nexusbank.feature.dashboard.ui.DashboardScreen
import com.example.nexusbank.feature.dashboard.ui.MoreOptionsScreen
import com.example.nexusbank.feature.onboarding.ui.SplashScreen
import com.example.nexusbank.feature.transactions.ui.TransactionsScreen
import com.example.nexusbank.feature.transfers.ui.ConfirmTransferScreen
import com.example.nexusbank.feature.transfers.ui.NewTransferScreen
import com.example.nexusbank.feature.transfers.ui.TransferSuccessScreen

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
                onSendMoneyClick = {
                    navController.navigate(Screen.NewTransfer.route)
                },
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
                onBackClick = { navController.popBackStack() },
                onOptionClick = { key ->
                    when (key) {
                        "transactions" -> navController.navigate(Screen.Transactions.route)
                        else -> { /* TODO */ }
                    }
                }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.NewTransfer.route) {
            NewTransferScreen(
                onBackClick = { navController.popBackStack() },
                onRecipientResolved = { fromAccountId, toAccount, resolved ->
                    navController.navigate(
                        Screen.ConfirmTransfer.build(
                            fromAccountId = fromAccountId,
                            toAccountNumber = toAccount,
                            holderName = resolved.holderName,
                            accountMasked = resolved.accountNumberMasked,
                            accountType = resolved.accountType
                        )
                    )
                }
            )
        }

        composable(
            route = Screen.ConfirmTransfer.route,
            arguments = listOf(
                navArgument("fromAccountId") { type = NavType.StringType },
                navArgument("toAccountNumber") { type = NavType.StringType },
                navArgument("holderName") { type = NavType.StringType },
                navArgument("accountMasked") { type = NavType.StringType },
                navArgument("accountType") { type = NavType.StringType }
            )
        ) {
            ConfirmTransferScreen(
                onBackClick = { navController.popBackStack() },
                onTransferSuccess = { result ->
                    navController.navigate(
                        Screen.TransferSuccess.build(
                            referenceNumber = result.referenceNumber,
                            amount = result.amount,
                            currency = result.currency,
                            recipientName = result.toName,
                            accountMasked = result.toAccountNumberMasked,
                            runningBalance = result.runningBalance,
                            completedAt = result.completedAt
                        )
                    ) {
                        popUpTo(Screen.NewTransfer.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.TransferSuccess.route,
            arguments = listOf(
                navArgument("referenceNumber") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType },
                navArgument("currency") { type = NavType.StringType },
                navArgument("recipientName") { type = NavType.StringType },
                navArgument("accountMasked") { type = NavType.StringType },
                navArgument("runningBalance") { type = NavType.StringType },
                navArgument("completedAt") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            TransferSuccessScreen(
                referenceNumber = args?.getString("referenceNumber").orEmpty(),
                amount = args?.getString("amount").orEmpty(),
                currency = args?.getString("currency").orEmpty(),
                recipientName = args?.getString("recipientName").orEmpty(),
                recipientAccountMasked = args?.getString("accountMasked").orEmpty(),
                runningBalance = args?.getString("runningBalance").orEmpty(),
                completedAt = args?.getString("completedAt").takeUnless { it.isNullOrBlank() },
                onDoneClick = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                }
            )
        }
    }
}
