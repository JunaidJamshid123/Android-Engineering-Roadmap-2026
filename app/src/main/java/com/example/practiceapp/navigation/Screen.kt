package com.example.practiceapp.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object MoreOptions : Screen("more_options")

    data object Transactions : Screen("transactions")

    data object NewTransfer : Screen("new_transfer")

    data object ConfirmTransfer : Screen(
        "confirm_transfer/{fromAccountId}/{toAccountNumber}/{holderName}/{accountMasked}/{accountType}"
    ) {
        fun build(
            fromAccountId: String,
            toAccountNumber: String,
            holderName: String,
            accountMasked: String,
            accountType: String
        ): String {
            val fid = android.net.Uri.encode(fromAccountId)
            val toAcc = android.net.Uri.encode(toAccountNumber)
            val n = android.net.Uri.encode(holderName)
            val m = android.net.Uri.encode(accountMasked)
            val t = android.net.Uri.encode(accountType)
            return "confirm_transfer/$fid/$toAcc/$n/$m/$t"
        }
    }

    data object TransferSuccess : Screen(
        "transfer_success/{referenceNumber}/{amount}/{currency}/{recipientName}/{accountMasked}/{runningBalance}/{completedAt}"
    ) {
        fun build(
            referenceNumber: String,
            amount: String,
            currency: String,
            recipientName: String,
            accountMasked: String,
            runningBalance: String,
            completedAt: String?
        ): String {
            val ref = android.net.Uri.encode(referenceNumber)
            val amt = android.net.Uri.encode(amount)
            val cur = android.net.Uri.encode(currency)
            val rn = android.net.Uri.encode(recipientName)
            val am = android.net.Uri.encode(accountMasked)
            val rb = android.net.Uri.encode(runningBalance)
            val ca = android.net.Uri.encode(completedAt.orEmpty())
            return "transfer_success/$ref/$amt/$cur/$rn/$am/$rb/$ca"
        }
    }
}
