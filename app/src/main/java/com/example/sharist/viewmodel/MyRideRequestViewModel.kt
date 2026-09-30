package com.example.sharist.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.RideRequest
import com.example.sharist.data.model.SimpleLocation
import com.example.sharist.data.model.UserProfile
import com.example.sharist.data.model.toStats
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.ReviewRepository
import com.example.sharist.data.repository.RideRequestRepository
import com.example.sharist.data.repository.SubscriptionRepository
import com.example.sharist.data.repository.UserRepository
import com.example.sharist.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.compareTo
import kotlin.time.Clock
import kotlin.time.Instant


data class NewRideRequestUiState(
    val userId:String?=null,
    val departure: SimpleLocation?=null,
    val destination: SimpleLocation?=null,
    val toleranceRange:Int=0,
    val date: Instant?=null,
    val isWeekly: Boolean=false,
    val isDaily: Boolean=false,
    var error: String? = null,
)


class MyRideRequestViewModel (application: Application): AndroidViewModel(application) {
    private val db = SharistDatabase.getDatabase(application)
    private val _targetProfile = MutableStateFlow<UserProfile?>(null)
    val targetProfile = _targetProfile.asStateFlow()

    private val _currentRideRequest = MutableStateFlow<RideRequest?>(null)
    val currentRideRequest = _currentRideRequest.asStateFlow()
    private val _availableRideRequests = MutableStateFlow(emptyList<RideRequest>())
    val availableRideRequests = _availableRideRequests.asStateFlow()
    val myRideRequests = SessionManager.myRideRequests

    private val requestRepository = RideRequestRepository(
        dao = db.rideRequestDao(),
        remote = StorageRemoteDataSource(),
    )
    private val userRepository = UserRepository(
        dao = db.userDao(),
        remote = StorageRemoteDataSource(),
        local = FileStorageDataSource(),
        authDao = db.authDao()
    )

    private val reviewRepository = ReviewRepository(
        remote = StorageRemoteDataSource(),
        dao = db.reviewDao()
    )
    private val subscriptionRepository = SubscriptionRepository(
        dao= db.subscriptionDao(),
        rideDao = db.rideDao(),
        rideRequestDao= db.rideRequestDao(),
        remote = StorageRemoteDataSource(),
    )
    val requestUiState = SessionManager.newRideRequestState


    fun validateRequest1(): Boolean{

        val state = requestUiState.value

        val now = Clock.System.now()

        var error: String? = null

        when {

            state.departure == null -> {
                error = "Initial location is required"
            }

            state.destination == null -> {
                error = "Destination location is required"
            }

            state.date == null -> {
                error = "Departure date is required"
            }

            state.date < now -> {
                error = "Departure date must be in the future"
            }

            state.toleranceRange <= 0 -> {
                error = "Tolerance range must be greater than 0"
            }

            state.departure == state.destination -> {
                error = "Departure and destination cannot be the same"
            }
        }

        if (error != null) {
            updateRequestError(error)
            return false
        }

        updateRequestError(null)

        return true
    }


    fun nextRequest1(onSuccess:()->Unit){
        if (!validateRequest1()){
            return
        }
        updateRequestError(null)
        onSuccess()
    }
    fun updateRequestDate(initialDate:Instant){
        SessionManager.updateNewRideRequest {
            it.copy(date=initialDate)
        }

    }

    private fun updateRequestError(error:String?){
        SessionManager.updateNewRideRequest {
            it.copy(error=error)
        }
    }

    fun updateRequestInitialLocation(location: Location){
        SessionManager.updateNewRideRequest {
            it.copy(
                departure =  convertLocationToSimpleLocation(location),
                error=null
            )
        }
    }

    fun updateRequestDestination(location:Location){
        SessionManager.updateNewRideRequest {
            it.copy(
                destination =  convertLocationToSimpleLocation(location),
                error=null
            )
        }
    }
    fun updateTolerance(tolerance:Int){
        SessionManager.updateNewRideRequest {
            it.copy(
                toleranceRange =  tolerance,
                error=null
            )
        }
    }
    fun updateWeekly(value:Boolean){
        SessionManager.updateNewRideRequest {
            it.copy(
                isWeekly = value,
                error=null
            )
        }
    }

    fun updateDaily(value:Boolean){
        SessionManager.updateNewRideRequest {
            it.copy(
                isDaily = value,
                error=null
            )
        }
    }

    private fun buildRideRequest(): RideRequest? {
        val state = requestUiState.value

        val userId = SessionManager.user.value?.id

        if (userId == null) {
            updateRequestError("User must be authenticated")
            return null
        }

        when {

            state.departure == null -> {
                updateRequestError("Initial location is required")
                return null
            }

            state.destination == null -> {
                updateRequestError("Destination location is required")
                return null
            }

            state.date == null -> {
                updateRequestError("Date is required")
                return null
            }

            state.date < Clock.System.now() -> {
                updateRequestError("Date must be in the future")
                return null
            }

            state.toleranceRange <= 0 -> {
                updateRequestError("Tolerance range must be greater than 0")
                return null
            }

            state.departure == state.destination -> {
                updateRequestError("Departure and destination cannot be the same")
                return null
            }

            state.isWeekly && state.isDaily -> {
                updateRequestError("Request cannot be both weekly and daily")
                return null
            }
        }

        updateRequestError(null)

        return RideRequest(
            id = UUID.randomUUID().toString(),
            userId = userId,
            departure = state.departure,
            destination = state.destination,
            toleranceRange = state.toleranceRange,
            date = state.date,
            isWeekly = state.isWeekly,
            isDaily = state.isDaily
        )
    }


    fun addRideRequest(context:Context, onSuccess:()->Unit){
        val rideRequest = buildRideRequest() ?: return
        viewModelScope.launch {
            requestRepository.addRideRequest(context ,rideRequest)
            SessionManager.updateNewRideRequest {
                NewRideRequestUiState()
            }
            onSuccess()
        }
    }

    fun loadRideRequest(rideRequestId:String) {
        viewModelScope.launch{
            _currentRideRequest.value = requestRepository.getRideRequest(rideRequestId)
        }
    }

    fun cancelAcceptedRequest(context:Context,request: RideRequest){
        viewModelScope.launch {
            subscriptionRepository.cancelRideRequest(context, request)
        }
    }

    fun acceptRequest(context:Context,request: RideRequest, userId:String, onSuccess: () -> Unit){
        Log.d("Subscribe", "User $userId, accepted request $request.id")
        viewModelScope.launch {
            subscriptionRepository.acceptRideRequest(context,request, userId)
            onSuccess()
        }
    }

    fun removeRideRequest(context:Context, rideRequest: RideRequest){
        viewModelScope.launch {
            requestRepository.removeRideRequest(context,rideRequest)
        }
    }
    fun stopRecurringRequest(context: Context, request: RideRequest){
        viewModelScope.launch(Dispatchers.IO) {
            requestRepository.stopRecurringRequest(context, request)
        }
    }


    fun loadTargetProfile(userId:String){
        Log.d("UserRepository", "Loading Profile")
        if (_targetProfile.value?.id == userId) return
        Log.d("UserRepository", "Is not loaded")
        viewModelScope.launch(Dispatchers.IO) {
            _targetProfile.value = try {
                userRepository.getProfile(getApplication(),userId)
            } catch (e: Exception) {
                Log.e("UserRepository", "Unable to load: ${e.toString()}")
                null
            }
            val reviews = reviewRepository.getReviews(userId)
            val stats = reviews.toStats()
            _targetProfile.update { current ->
                current?.copy(averageRating = stats.average)
            }
        }
    }

}