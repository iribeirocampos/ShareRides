package com.example.sharist.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.PrimaryKey
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.model.Review
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.model.UserProfile
import com.example.sharist.data.model.Vehicle
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.ReviewRepository
import com.example.sharist.data.repository.UserRepository
import com.example.sharist.data.repository.VehicleRepository
import com.example.sharist.ui.components.ReviewCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import java.util.UUID

class UserDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SharistDatabase.getDatabase(application)
    private val remote = StorageRemoteDataSource()
    private val local = FileStorageDataSource()
    private val userRepository = UserRepository(
        dao = db.userDao(),
        remote = remote,
        local = local,
        authDao = db.authDao()
    )
    private val vehicleRepository = VehicleRepository(
        remote = remote,
        local = local,
        dao = db.vehicleDao()
    )
    private val reviewRepository = ReviewRepository(
        remote=remote,
        dao = db.reviewDao()
    )



    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile = _profile.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles = _vehicles.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews = _reviews.asStateFlow()


    fun load(userId: String) {
        Log.d("LOADING", "Loading User profile $userId")
        viewModelScope.launch(Dispatchers.IO) {
            _profile.value = try {
                userRepository.getProfile(getApplication(),userId)
            } catch (e: Exception) {
                Log.e("UserRepository", "Unable to load user ${e.toString()}")
                null
            }
            _vehicles.value = try {
                // Local-first with remote fallback + Room caching, same path as MyVehicles.
                vehicleRepository.getVehicles(userId)
            } catch (e: Exception) {
                emptyList()
            }
            _reviews.value = try{
                reviewRepository.getReviews(userId)
            }catch(e: Exception){
                emptyList()
            }
            Log.d("IMAGE", "Loaded userprofile ${_profile.value}")
        }
    }

}
