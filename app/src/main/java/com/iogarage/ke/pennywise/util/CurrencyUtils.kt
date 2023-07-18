package com.iogarage.ke.pennywise.util

import com.iogarage.ke.pennywise.domain.entity.TransactionType
import java.math.RoundingMode
import java.text.DecimalFormat


const val format = "#,##0.00"

fun Long.toCurrency(transactionType: TransactionType): String {
    val df = DecimalFormat(format)
    df.roundingMode = RoundingMode.DOWN
    val formatted = df.format(this)
    return if (transactionType == TransactionType.BORROWING) "+$formatted" else "-$formatted"
}

fun Long.toCurrency(): String {
    val df = DecimalFormat(format)
    df.roundingMode = RoundingMode.DOWN
    return df.format(this)
}

fun Double.toCurrency(): String {
    val df = DecimalFormat(format)
    df.roundingMode = RoundingMode.DOWN
    return df.format(this)
}

fun String.toCurrency(): String {
    val df = DecimalFormat(format)
    df.roundingMode = RoundingMode.UP
    return df.format(this.toDouble())
}



