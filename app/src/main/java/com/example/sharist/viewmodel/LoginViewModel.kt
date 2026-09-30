package com.example.sharist.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.local.util.LocationProvider
import com.example.sharist.data.model.UserType
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.LocationRepository
import com.example.sharist.data.repository.RideRepository
import com.example.sharist.data.repository.RideRequestRepository
import com.example.sharist.data.repository.SubscriptionRepository
import com.example.sharist.data.repository.UserRepository
import com.example.sharist.data.repository.VehicleRepository
import com.example.sharist.data.session.SessionManager
import com.example.sharist.sync.SyncManager
import com.example.sharist.sync.ValidationManager
import com.example.sharist.sync.WeatherSyncManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val error: String?=null
)

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession = _isCheckingSession.asStateFlow()
    private val db = SharistDatabase.getDatabase(application)
    private val remote = StorageRemoteDataSource()
    private val local = FileStorageDataSource()
    private val repository = UserRepository(
        dao = db.userDao(),
        remote = remote,
        local = local,
        authDao = db.authDao()
    )
    private val locationRepository = LocationRepository(
        dao = db.locationDao(),
        remote = remote,
        local = local
    )

    private val rideRepository = RideRepository(
        dao=db.rideDao(),
        remote=remote
    )
    private val rideRequestRepository = RideRequestRepository(
        dao=db.rideRequestDao(),
        remote=remote
    )

    private val vehicleRepository = VehicleRepository(
        dao = db.vehicleDao(),
        local = local,
        remote = remote
    )

    private val subscriptionRepository = SubscriptionRepository(
        remote = remote,
        dao = db.subscriptionDao(),
        rideDao =  db.rideDao(),
        rideRequestDao = db.rideRequestDao()
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()


   suspend fun loadUserData(userId:String){
        val location = LocationProvider(getApplication()).getCurrentLocation()
       Log.d("LOCATION", "Got the current location ${location.toString()}")
        repository.loadUserProfileAsync(getApplication(),userId)
        locationRepository.loadUserLocationsAsync(userId)
        if (SessionManager.user.value?.type == UserType.DRIVER){
            rideRepository.loadUserRidesAsync(userId)
            vehicleRepository.loadMyVehiclesAsync(userId)
            rideRequestRepository.loadAvailableRideRequests(getApplication(), location)
            subscriptionRepository.loadMyAcceptedRideRequests(userId)
        }else{
            rideRequestRepository.loadUserRideRequestsAsync(userId)
            subscriptionRepository.loadMyRideSubscriptions(userId)
            rideRepository.loadAvailableRides(getApplication(), location)
        }
    }
    fun checkSession(){
        viewModelScope.launch(Dispatchers.IO) {
            val userId= repository.restoreSessionOffline()
            if (userId==""){
                UiEventBus.send(UiEvent.Warning("No User session found, Unable to load, please Login"))
                _isCheckingSession.value = false
                return@launch
            }
            Log.d("LOGIN", "loading data")
            loadUserData(userId)
            _isCheckingSession.value = false
        }
    }
    fun login(onSuccess: () -> Unit){
        viewModelScope.launch {
            try {
                repository.login(
                        email = _uiState.value.email,
                        password = _uiState.value.password
                    )
                    val userId = SessionManager.session.value
                                ?.user
                                ?.id
                    if (userId==null) {
                        UiEventBus.send(UiEvent.Error("No User session found, Unable to load, please Login"))
                        return@launch
                    }
                   loadUserData(userId)
                WeatherSyncManager.triggerImmediateWeather(getApplication()) // runs an immediate Check
                SyncManager.enqueueSync(getApplication()) // Tries to Sync eventual objects that are not synced
                WeatherSyncManager.enqueueWeatherSync(getApplication()) // Starts Weather checks every 12 hours
                ValidationManager.triggerImmediateValidation(getApplication())
                ValidationManager.enqueueValidation(getApplication()) // Starts validation checks every 15 minutes
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
             }
        }
    }
    fun updateEmail(value: String) {
        _uiState.update {
            it.copy(email = value)
        }
    }

    fun updatePassword(value: String) {
        _uiState.update {
            it.copy(password = value)
        }
    }
}