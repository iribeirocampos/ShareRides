package com.example.sharist.data.policies

import com.example.sharist.data.model.Review
import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.UserProfile
import kotlin.reflect.KClass

object CachePolicy {
    private val ttlMap: Map<KClass<*>, Long> = mapOf(
        List::class to (15 * 60 * 1000L),      // fallback for lists
        Review::class to (15 * 60 * 1000L),
        UserProfile::class to (30 * 60 * 1000L), // 30 min cache validation - USer does not change that much
        // Ride::class         to (5 * 60 * 1000L)
     //   Vehicle::class to (10 * 60 * 1000L)
    )

    fun <T : Any> ttlFor(clazz: KClass<T>): Long =
        ttlMap[clazz] ?: (15 * 60 * 1000L) // default 15 min
}