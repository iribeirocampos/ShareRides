package com.example.sharist.data.local.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun formatDateTime(dt: Instant?): String {
    if (dt == null) return ""

    val local = dt.toLocalDateTime(TimeZone.currentSystemDefault())

    return "%04d-%02d-%02d %02d:%02d".format(
        local.year,
        local.monthNumber,
        local.day,
        local.hour,
        local.minute
    )
}