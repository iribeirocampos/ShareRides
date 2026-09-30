package com.example.sharist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.sharist.data.model.Review


@Dao
interface ReviewDao {
    @Query("SELECT * FROM review")
    fun getAllReviews(): Flow<List<Review>>

    @Query("SELECT * FROM review WHERE targetUserId = :id AND syncState!='PENDING_DELETE'")
    suspend fun getUserReviews(id:String): List<Review>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Update
    suspend fun updateReview(review:Review)

    @Query("SELECT * FROM review WHERE syncState == 'PENDING_CREATE'")
    suspend fun getPendingReviews(): List<Review>
}
