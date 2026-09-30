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
import com.example.sharist.data.model.SubscribedRide
import com.example.sharist.data.model.UserProfile
import com.example.sharist.data.model.Vehicle
import com.example.sharist.data.model.toStats
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.ReviewRepository
import com.example.sharist.data.repository.RideRepository
import com.example.sharist.data.repository.RideRequestRepository
import com.example.sharist.data.repository.SubscriptionRepository
import com.example.sharist.data.repository.UserRepository
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class NewRideUiState(
    val initialLocation: SimpleLocation? = null,
    val destinationLocation: SimpleLocation? = null,

    val departureDateTime: Instant? = null,
    val arrivalDateTime: Instant? = null,

    val cost: Double = 0.0,
    val capacity: Int = 0,
    val cancelUpToDays: Int = 0,

    val isWeekly: Boolean = false,
    val isDaily: Boolean = false,

    var error: String? = null,
)


fun convertLocationToSimpleLocation(location:Location):SimpleLocation{
    return SimpleLocation(
        id = location.id,
        address = location.address,
        latitude = location.latitude,
        longitude = location.longitude
    )
}


class MyRideViewModel (application: Application): AndroidViewModel(application) {
    private val db = SharistDatabase.getDatabase(application)
    private val repository = RideRepository(
        dao = db.rideDao(),
        remote = StorageRemoteDataSource(),
    )


    private val subscriptionRepository = SubscriptionRepository(
        dao= db.subscriptionDao(),
        rideDao = db.rideDao(),
        rideRequestDao= db.rideRequestDao(),
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

    private val _targetProfile = MutableStateFlow<UserProfile?>(null)
    val targetProfile = _targetProfile.asStateFlow()

    val uiState = SessionManager.newRideState
    val myRides = SessionManager.myRides

    private val _currentRide = MutableStateFlow<Ride?>(null)
    val currentRide = _currentRide.asStateFlow()


    fun validateNext1(): Boolean{
        val now = Clock.System.now()
        val departure = uiState.value.departureDateTime
        if (uiState.value.initialLocation==null){
            updateError("Initial Location cannot be empty")
        }
        else if (departure == null|| departure < now) {
            Log.d("Iuri", departure.toString())
            updateError("Departure date is required or must be in the future")
            return false
        }
        if (uiState.value.error!= null) {
            return false
        }
        return true
        }


    fun getFirstVehicle(): Vehicle? {
        return SessionManager.myVehicles.value.firstOrNull()
    }
    fun validateNext2(): Boolean{
        val now = Clock.System.now()
        val arrival = uiState.value.arrivalDateTime
        if (uiState.value.destinationLocation==null){
            updateError("Destination Location cannot be empty")
        }
        else if (arrival == null || arrival < now) {
            updateError("Departure date is required")
            return false
        }

        else uiState.value.departureDateTime?.let {
            if(arrival<= it){
                updateError("Departure Date should be after the departure date")
                return false
            }
        }
        if (uiState.value.error != null){
            return false
        }
        updateError(null)
        return true
    }

    fun next1(onSuccess: () -> Unit){

        if (!validateNext1() || !validateNext2()){
            return
        }
        updateError(null)
        onSuccess()
    }

    fun updateInitialLocation(location:Location){
        SessionManager.updateNewRide {
            it.copy(
                initialLocation =  convertLocationToSimpleLocation(location),
                error=null
            )
        }

    }



    fun updateDepartureDateTime(departureDatetime: Instant){
        SessionManager.updateNewRide {
            it.copy(
                departureDateTime=departureDatetime,
                error = null
            )
        }
    }

    fun updateArrivalDateTime(arrivalDatetime: Instant){
        SessionManager.updateNewRide {
            it.copy(
                arrivalDateTime=arrivalDatetime,
                error = null
            )
        }
    }

    fun updateDestinationLocation(location:Location){
        SessionManager.updateNewRide {
            it.copy(
                destinationLocation = convertLocationToSimpleLocation(location),
                error=null
            )
        }
    }


    fun updateCost(cost:Double){
        SessionManager.updateNewRide {
            it.copy(cost=cost, error=null)
        }
    }
    fun updateCapacity(capacity:Int){
        SessionManager.updateNewRide {
            it.copy(
                capacity=capacity,
                error=null
            )
        }
    }

    fun updateCancelUpToDays(days:Int){
        SessionManager.updateNewRide {
            it.copy(
                cancelUpToDays = days,
                error=null
            )
        }
    }

    fun updateWeeklyRepetition(check: Boolean){
        SessionManager.updateNewRide {
            it.copy(isWeekly = check, error=null)

        }
    }

    fun updateDailyRepetition(check:Boolean){
        SessionManager.updateNewRide {
            it.copy(isDaily = check, error=null)
        }
    }


    private fun updateError(error:String?){
        SessionManager.updateNewRide {
            it.copy(error=error)
        }
    }


    private fun buildRide(): Ride? {
        val userId = SessionManager.user.value?.id
        val departure = uiState.value.initialLocation
        val destination =uiState.value.destinationLocation
        if (userId == null) {
            updateError("User must be authenticated")
            return null
        }

        when {
            departure == null -> {
                updateError("Initial location is required")
                return null
            }

            destination == null -> {
                updateError("Destination location is required")
                return null
            }

            uiState.value.arrivalDateTime == null -> {
                updateError("Arrival Date is required")
                return null
            }

            uiState.value.departureDateTime == null -> {
                updateError("Departure Date is required")
                return null
            }

            uiState.value.cost <= 0.0 -> {
                updateError("Cost must be greater than 0")
                return null
            }

            uiState.value.capacity <= 0 -> {
                updateError("Available seats must be greater than 0")
                return null
            }
            getFirstVehicle() == null -> {
                updateError("No Vehicles for Driver")
                return null
            }
        }

        updateError(null)

        return Ride(
            id = UUID.randomUUID().toString(),
            departure = departure,
            destination = destination,
            departureDateTime = uiState.value.departureDateTime,
            arrivalDateTime = uiState.value.arrivalDateTime,
            cost = uiState.value.cost,
            availableSeats = uiState.value.capacity,
            vehicleId = getFirstVehicle()!!.id,
            userId = userId,
            isWeekly = uiState.value.isWeekly,
            isDaily = uiState.value.isDaily,
            cancelUpToDays = uiState.value.cancelUpToDays
        )
    }
    fun addRide(context: Context, onSuccess:()->Unit){
        val ride = buildRide() ?: return
        viewModelScope.launch {
        repository.addRide(context ,ride)
            SessionManager.updateNewRide {
                NewRideUiState()
            }
        onSuccess()
        }
    }

    fun removeRide(context:Context, ride:Ride){
        viewModelScope.launch {
            repository.removeRide(context,ride)
        }
    }



    fun loadRide(rideId:String) {
        viewModelScope.launch{
           _currentRide.value = repository.getRide(rideId)
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
            Log.d("IMAGE", "Loaded profile ${_targetProfile.value}")
        }

    }

    fun subscribeRide(context:Context , rideId:String, rain: Boolean, onSuccess: () -> Unit){
        val userId = SessionManager.user.value?.id
        viewModelScope.launch(Dispatchers.IO){
            if (userId==null){
                UiEventBus.send(UiEvent.Error("Unable to Subscribe, please make sure you are authenticated"))
                return@launch
            }
            subscriptionRepository.subscribeRide(context, rideId, userId, rain )
        }
        Log.d("Subscribe", "Subscribe button pressed for ride $rideId, with rain $rain")
        onSuccess()
    }

    fun stopRecurringRide(context: Context, ride: Ride){
        viewModelScope.launch(Dispatchers.IO) {
            repository.stopRecurringRide(ride)
        }
    }

    fun cancelSubscription(sub: SubscribedRide){
        viewModelScope.launch {
            subscriptionRepository.unsubscribeRide(sub)
        }

    }

}