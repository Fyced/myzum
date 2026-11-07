package com.example.myzum.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    fun formatAmount(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = ','
        symbols.groupingSeparator = '.'
        val df = DecimalFormat("#,##0.00", symbols)
        return "${df.format(amount)} €"
    }

    fun parseAmount(input: String): Double? {
        return try {
            input.replace(".", "").replace(",", ".").toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
