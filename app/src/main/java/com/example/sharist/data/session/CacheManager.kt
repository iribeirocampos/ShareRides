package com.example.sharist.data.session

import android.util.Log
import com.example.sharist.data.policies.CachePolicy
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


data class CacheEntry<T>(
    val data: T,
    val lastUpdated: Long,
    val lastAccessed: Long,
    val ttlMillis: Long
)
object CacheManager {

    // One map per type, keyed by the class itself
    @PublishedApi
    internal val caches = ConcurrentHashMap<KClass<*>, ConcurrentHashMap<String, CacheEntry<*>>>()

    @PublishedApi
    internal fun <T : Any> cacheFor(clazz: KClass<T>): ConcurrentHashMap<String, CacheEntry<T>> {
        @Suppress("UNCHECKED_CAST")
        return caches.getOrPut(clazz) { ConcurrentHashMap() }
                as ConcurrentHashMap<String, CacheEntry<T>>
    }

    inline fun <reified T : Any> put(key: String, value: T) {
        Log.d("CACHE", "Putting in cache ${T::class.toString()}")
        val now = System.currentTimeMillis()
        cacheFor(T::class)[key] = CacheEntry(
            data = value,
            lastUpdated = now,
            lastAccessed = now,
            ttlMillis = CachePolicy.ttlFor(T::class)
        )
    }

    inline fun <reified T : Any> putList(key: String, values: List<T>) {
        Log.d("CACHE", "Putting list cache ${T::class}")
        val now = System.currentTimeMillis()
        val typedKey = "${T::class.qualifiedName}:$key"
        cacheFor(List::class as KClass<List<T>>)[typedKey] = CacheEntry(
            data = values,
            lastUpdated = now,
            lastAccessed = now,
            ttlMillis = CachePolicy.ttlFor(T::class) // uses the element type's TTL
        )
    }

    inline fun <reified T : Any> get(key: String): T? {
        Log.d("CACHE", "Getting cache ${T::class.toString()}")
        val cache = cacheFor(T::class)
        val entry = cache[key] ?: return null
        Log.d("CACHE", "Have results, checking if still TTL")
        if (System.currentTimeMillis() - entry.lastUpdated > entry.ttlMillis) {
            cache.remove(key)
            return null
        }

        cache[key] = entry.copy(lastAccessed = System.currentTimeMillis())
        return entry.data
    }

    inline fun <reified T : Any> remove(key: String) {
        cacheFor(T::class).remove(key)
    }

    inline fun <reified T : Any> invalidateAll() {
        caches.remove(T::class)
    }
    inline fun <reified T : Any> getList(key: String): List<T>? {
        @Suppress("UNCHECKED_CAST")
        val cache = cacheFor(List::class as KClass<List<T>>)
        val typedKey = "${T::class.qualifiedName}:$key"
        val entry = cache[typedKey] ?: return null

        if (System.currentTimeMillis() - entry.lastUpdated > entry.ttlMillis) {
            cache.remove(typedKey)
            return null
        }

        cache[typedKey] = entry.copy(lastAccessed = System.currentTimeMillis())
        return entry.data
    }

    inline fun <reified T : Any> getAll(): List<T> {
        return cacheFor(T::class).values
            .filter { System.currentTimeMillis() - it.lastUpdated <= it.ttlMillis }
            .map { it.data }
    }
}