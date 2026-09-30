package com.example.sharist.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.model.Review
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.ReviewRepository
import com.example.sharist.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


data class NewReviewUiState(
    val targetUserId: String="",
    val reviewerId: String="",
    val reviewerName: String="",
    val rating: Int=0, // 1..5
    val comment: String="",
    val error:String="",
    val syncState: String = SyncState.PENDING_CREATE.toString()
)

fun NewReviewUiState.toReviewOrNull(): Review? {
    if (targetUserId.isBlank()) return null
    if (reviewerId.isBlank()) return null
    if (reviewerName.isBlank()) return null
    if (rating !in 1..5) return null

    return Review(
        id=UUID.randomUUID().toString(),
        targetUserId = targetUserId,
        reviewerId = reviewerId,
        reviewerName = reviewerName,
        rating = rating,
        comment = comment,
        syncState = syncState
    )
}
class MakeReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SharistDatabase.getDatabase(application)
    private val remote = StorageRemoteDataSource()
    private val reviewRepository = ReviewRepository(remote, db.reviewDao())
    private val _newReviewUiState = MutableStateFlow(NewReviewUiState())
    val newReview = _newReviewUiState.asStateFlow()


    fun setUsersId(targetUserId:String){
        val authUser = SessionManager.user.value
        if (authUser ==null){
            _newReviewUiState.update { state ->
                    state.copy(error = "No authenticated user was found. Please Sign In")
                }
        }else{
        _newReviewUiState.update { state ->
            state.copy(reviewerId = authUser.id, targetUserId = targetUserId, reviewerName = authUser.username)
            }
        }
    }


    fun updateComment(comment:String){
        Log.d("Review", "Updating comment to $comment")
        _newReviewUiState.update {
                state ->
            state.copy(comment = comment)
        }
    }


    fun updateRating(rating:Int){
        Log.d("Review", "Updating rating to $rating")
        _newReviewUiState.update {
                state ->
            state.copy(rating = rating)
        }
    }

    fun addReview(context: Context, onSuccess:()->Unit) {
        viewModelScope.launch {
            val review = _newReviewUiState.value.toReviewOrNull()
            if (review == null) {
                _newReviewUiState.update { state ->
                    state.copy(error = "Please complete all fields correctly (rating must be 1–5).")
                }
                return@launch
            }
            // clear error when valid
            _newReviewUiState.update { it.copy(error = "") }
            reviewRepository.addReview(context, review)
            onSuccess()
        }
    }
}
