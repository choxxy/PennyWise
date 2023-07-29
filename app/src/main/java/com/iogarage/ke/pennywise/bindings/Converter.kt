package com.iogarage.ke.pennywise.bindings

import androidx.databinding.InverseMethod
import com.iogarage.ke.pennywise.util.asDateString
import com.iogarage.ke.pennywise.util.toCurrency
import com.iogarage.ke.pennywise.util.toLocalDate

object Converter {
    @InverseMethod("stringToDate")
    @JvmStatic
    fun dateToString(value: Long): String {
        return if (value == 0L) return "" else value.asDateString()
    }

    @JvmStatic
    fun stringToDate(value: String): Long {
        return if (value.isEmpty())
            0
        else value.toLocalDate("E, dd MMM, yyyy").toEpochDay()
    }


    @InverseMethod("currencyToDouble")
    @JvmStatic
    fun doubleToCurrency(value: Double): String {
        return value.toString()//toCurrency()
    }

    @JvmStatic
    fun currencyToDouble(value: String): Double {
        val formatted = value.replace(",", "")
        return formatted.toDouble()
    }
}