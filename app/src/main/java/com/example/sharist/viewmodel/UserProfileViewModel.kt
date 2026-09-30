package com.example.sharist.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.local.util.isValidEmail
import com.example.sharist.data.local.util.normalizeEmail
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.UserRepository
import com.example.sharist.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.sharist.data.repository.ReviewRepository
import com.example.sharist.sync.SyncManager
import com.example.sharist.sync.ValidationManager
import com.example.sharist.sync.WeatherSyncManager
import com.example.sharist.data.model.Review
import kotlinx.coroutines.Dispatchers

data class UserProfileState(
    val name: String = "",
    val email: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

class UserProfileViewModel( application: Application) : AndroidViewModel(application){

    private val db = SharistDatabase.getDatabase(application)
    private val repository = UserRepository(
        dao = db.userDao(),
        remote = StorageRemoteDataSource(),
        local = FileStorageDataSource(),
        authDao = db.authDao()
    )
    private val reviewRepository = ReviewRepository(
        remote = StorageRemoteDataSource(),
        dao = db.reviewDao()
        )
    private val _userProfileState = MutableStateFlow(UserProfileState())
    val uiState = _userProfileState.asStateFlow()

    private val _reviews = MutableStateFlow( emptyList<Review>())
    val reviews = _reviews.asStateFlow()

    private var seededUserId: String? = null

    init {
        // Seed the editable fields whenever the active user changes (first load or
        // log out → log in as a different user). Re-emissions for the same user
        // (e.g. after a username save) are ignored so in-progress edits aren't clobbered.
        viewModelScope.launch(Dispatchers.IO) {
            SessionManager.user.collect { profile ->
                if (profile != null && profile.id != seededUserId) {
                    seededUserId = profile.id
                    _userProfileState.update {
                        it.copy(name = profile.username, email = getEmail())
                    }
                    _reviews.value = reviewRepository.getReviews(profile.id)
                }
            }

        }
    }

    fun updateName(name: String) {
        _userProfileState.update { it.copy(name = name) }
    }

    fun updateEmail(email: String) {
        _userProfileState.update { it.copy(email = email) }
    }

    private fun updateError(error: String) {
        _userProfileState.update { it.copy(error = error, successMessage = null) }
    }

    fun clearMessages() {
        _userProfileState.update { it.copy(error = null, successMessage = null) }
    }

    fun saveProfile(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val state = _userProfileState.value
            val profile = SessionManager.user.value

            if (state.name.isBlank()) {
                updateError("Name cannot be empty")
                return@launch
            }
            if (!isValidEmail(state.email)) {
                updateError("Email is not in correct format")
                return@launch
            }

            val newEmail = normalizeEmail(state.email)
            val emailChanged = newEmail != normalizeEmail(getEmail())
            val nameChanged = profile != null && state.name != profile.username

            if (!emailChanged && !nameChanged) {
                updateError("No changes to save")
                return@launch
            }

            try {
                if (nameChanged) {
                    repository.usernameChange(state.name)
                }
                if (emailChanged) {
                    repository.emailChange(newEmail)
                }
            } catch (e: Exception) {
                updateError(e.message ?: "Failed to update profile")
                return@launch
            }

            val message = when {
                emailChanged && nameChanged ->
                    "Profile updated. Check your inbox to confirm your new email."
                emailChanged ->
                    "Check your inbox to confirm your new email."
                else -> "Profile updated"
            }
            _userProfileState.update {
                it.copy(error = null, successMessage = message)
            }
            onSuccess()
        }
    }

    fun updatePhotoUri(context:Context, uri: Uri) {
        viewModelScope.launch {
            repository.updatePhoto(context, uri)
            }

        }

    fun logOff(context:Context,onSuccess:()->Unit){
        viewModelScope.launch {
            SessionManager.terminateSession()
            repository.clearSession()
            stopWorkers(context)
            onSuccess()
        }

    }
    fun getEmail():String {
        return StorageRemoteDataSource().getCurrentSession()?.user?.email ?: ""
    }

    fun stopWorkers(context: Context){
        SyncManager.cancel(context)
        ValidationManager.cancel(context)
        WeatherSyncManager.cancel(context)
    }
}