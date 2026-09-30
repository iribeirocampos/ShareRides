package com.example.sharist.data.model

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant

class Converters {

    private val json = kotlinx.serialization.json.Json

    @TypeConverter
    fun fromSimpleLocation(value: SimpleLocation): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toSimpleLocation(value: String): SimpleLocation {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromInstant(value: Instant?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toInstant(value: String?): Instant? {
        return value?.let { Instant.parse(it) }
    }
}