package com.example.nexusbank.core.common.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun isToday(timestamp: Long): Boolean {
        val today = dateFormat.format(Date())
        val date = dateFormat.format(Date(timestamp))
        return today == date
    }
}

object CurrencyUtils {
    fun formatAmount(amount: Double, currencyCode: String = "PKR"): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        try {
            format.currency = Currency.getInstance(currencyCode)
        } catch (_: Exception) { }
        return format.format(amount)
    }

    fun formatAmountPlain(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)
    }
}

object MaskUtils {
    fun maskAccountNumber(accountNumber: String): String {
        if (accountNumber.length < 4) return accountNumber
        val last4 = accountNumber.takeLast(4)
        val masked = "●".repeat(accountNumber.length - 4)
        return "$masked$last4"
    }

    fun maskCardNumber(cardNumber: String): String {
        if (cardNumber.length < 4) return cardNumber
        val last4 = cardNumber.takeLast(4)
        return "**** **** **** $last4"
    }

    fun maskPhone(phone: String): String {
        if (phone.length < 4) return phone
        val last4 = phone.takeLast(4)
        return "***${last4}"
    }
}
