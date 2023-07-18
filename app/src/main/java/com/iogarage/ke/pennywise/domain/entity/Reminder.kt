package com.iogarage.ke.pennywise.domain.entity

data class Reminder(
    val title: String = "",
    val content: String = "",
    val dateAndTime: Long = 0,
    val active: Boolean = false,
    val numberShown: Int = 0,
)