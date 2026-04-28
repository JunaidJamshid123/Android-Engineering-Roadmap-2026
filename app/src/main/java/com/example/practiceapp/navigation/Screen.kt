package com.example.practiceapp.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Accounts : Screen("accounts")
    data object Cards : Screen("cards")
    data object Transactions : Screen("transactions/{accountId}") {
        fun createRoute(accountId: String) = "transactions/$accountId"
    }
    data object Profile : Screen("profile")
    data object Loans : Screen("loans")
    data object Notifications : Screen("notifications")
    data object Kyc : Screen("kyc")
    data object Investments : Screen("investments")
}
