package com.example.sharist.data.repository

import android.content.Context
import android.util.Log
import com.example.sharist.data.local.dao.ReviewDao
import com.example.sharist.data.model.Review
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.session.CacheManager
import com.example.sharist.sync.SyncManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus

class ReviewRepository(
    private val remote: StorageRemoteDataSource,
    private val dao: ReviewDao
){
    suspend fun markSynced(review: Review) {
        dao.updateReview(review.copy(syncState = SyncState.SYNCED.toString()))
    }

    suspend fun getReviews(userId: String): List<Review> {
        CacheManager.getList<Review>(userId)?.let { return it } // Cache Hit
        Log.d("CACHE","Cache not hit, getting local results")
        val reviews = dao.getUserReviews(userId)
        val updatedReviews = try {
                val remoteReviews = remote.getUserReviews(userId) //  Getting the server reviews
            if (remoteReviews.size>reviews.size){
                Log.d("CACHE","Server is higher")
                remoteReviews // If server reviews is higher, update
                remoteReviews.forEach { review->dao.insertReview(review) }
                remoteReviews
                     }else{
                Log.d("CACHE","Local is same of supabase")
                         reviews // same information, no update needed
                     }
                }catch (e:Exception){
            Log.d("CACHE","Unable to get su+pabase values, using local")
            reviews //Unable to get supabase information, using local
        }
        CacheManager.putList(userId, updatedReviews)
        return updatedReviews
    }

    suspend fun addReview(context: Context, review: Review){
        dao.insertReview(review)
        val current = CacheManager.getList<Review>(review.targetUserId) ?: emptyList()
        CacheManager.putList(review.targetUserId, current + review)
        val synced = try {
            remote.insertReview(review)
        } catch (e: Exception) {
            null
        }
        if (synced != null) {
            markSynced(review)
            UiEventBus.send(UiEvent.Success("New Review Added"))
        }else{
            Log.d("WORKER", "OFFLINE creation")
            UiEventBus.send(UiEvent.Warning("Unable to upload the review, please confirm you have a connection"))
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }
}