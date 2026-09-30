package com.example.sharist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "review")
data class Review(
    @PrimaryKey val id: String,
    val targetUserId: String,
    val reviewerId: String,
    val reviewerName: String,
    val rating: Int, // 1..5
    val comment: String,
    val syncState: String = SyncState.PENDING_CREATE.toString()
)

fun List<Review>.toRatingMap(): Map<Int, Int> {
    return this.groupingBy { it.rating }
        .eachCount()
}


data class ReviewStats(
    val average: Float,
    val total: Int,
    val histogram: Map<Int, Int>
)

fun List<Review>.toStats(): ReviewStats {
    val histogram = groupingBy { it.rating }.eachCount()
    val average = if (isEmpty()) 0f else sumOf { it.rating }.toFloat() / size

    return ReviewStats(
        average = average,
        total = size,
        histogram = histogram
    )
}