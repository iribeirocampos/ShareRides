package com.example.sharist.data.session

import android.util.Log
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.RideRequest
import com.example.sharist.data.model.SimpleLocation
import com.example.sharist.data.model.SubscribedRide
import com.example.sharist.data.model.Subscription
import com.example.sharist.data.model.UserProfile
import com.example.sharist.data.model.Vehicle
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.remote.SupabaseClient
import com.example.sharist.viewmodel.NewRideRequestUiState
import com.example.sharist.viewmodel.NewRideUiState
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


private data class LoadedArea(
    val location: SimpleLocation,
    val radiusKm: Double
)

object SessionManager {
    private val _session = MutableStateFlow<UserSession?>(null)
    val session = _session.asStateFlow()
    private val _newRideState = MutableStateFlow(NewRideUiState())
    val newRideState = _newRideState.asStateFlow()
    private val _newRideRequestState = MutableStateFlow(NewRideRequestUiState())
    val newRideRequestState = _newRideRequestState.asStateFlow()
    private val _myVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val myVehicles = _myVehicles.asStateFlow()
    private val _user = MutableStateFlow<UserProfile?>(null)
    val user = _user.asStateFlow()
    private val _myLocations = MutableStateFlow<List<Location>>(emptyList())
    val myLocations = _myLocations.asStateFlow()
    private val _myRides = MutableStateFlow<List<Ride>>(emptyList())
    val myRides = _myRides.asStateFlow()
    private val _myRideRequests = MutableStateFlow<List<RideRequest>>(emptyList())
    val myRideRequests = _myRideRequests.asStateFlow()
    private val _myRideSubscriptions = MutableStateFlow<List<SubscribedRide>>(emptyList())
    val myRideSubscriptions = _myRideSubscriptions.asStateFlow()
    private val _myRideRequestSubscriptions = MutableStateFlow<List<RideRequest>>(emptyList())
    val myRideRequestSubscriptions = _myRideRequestSubscriptions.asStateFlow()

    private var lastLoadedArea: LoadedArea? = null
    val isLoggedIn: StateFlow<Boolean> =
        _session.map { it != null }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Default),
                started = SharingStarted.Eagerly,
                initialValue = false
            )
    fun insertRideRequest(rideRequest: RideRequest){
        _myRideRequests.value += rideRequest
    }
    fun removeRideRequest(rideRequest: RideRequest){
        _myRideRequests.value= _myRideRequests.value.filter { loc -> loc.id != rideRequest.id }
    }
    fun updateRideRequest(rideRequest: RideRequest){
        _myRideRequests.value = _myRideRequests.value.map { if (it.id == rideRequest.id) rideRequest else it }
    }
    fun updateNewRide(block: (NewRideUiState) -> NewRideUiState) {
        _newRideState.value = block(_newRideState.value)
    }
    fun updateNewRideRequest(block: (NewRideRequestUiState) -> NewRideRequestUiState) {
        _newRideRequestState.value = block(_newRideRequestState.value)
    }
    fun addRide(ride:Ride){
        _myRides.value += ride
    }
    fun removeRide(ride:Ride){
        _myRides.value= _myRides.value.filter { loc -> loc.id != ride.id }
    }
    fun getLocation(locationId:String):Location?{
        return _myLocations.value.firstOrNull { it.id == locationId }
    }
    fun addLocation(location:Location){
        _myLocations.value += location
    }

    fun removeLocation(location:Location){
        _myLocations.value= _myLocations.value.filter { loc -> loc.id != location.id }
    }
    fun setUser(profile: UserProfile) {
        _user.value = profile
    }


    fun clear() {
        _user.value = null
        _myLocations.value = emptyList()
        _myRides.value =emptyList()
        _myVehicles.value = emptyList()
        _myRideRequests.value = emptyList()
        _myRideSubscriptions.value  =emptyList()
        _myRideRequestSubscriptions.value = emptyList()
        clearSession()
    }


    suspend fun terminateSession(){
        StorageRemoteDataSource().signOut()
        clear()
    }

    fun setLocations(locations:List<Location>){
        _myLocations.value = locations
    }

    fun addVehicle(vehicle: Vehicle){
        _myVehicles.value += vehicle
    }

    fun removeVehicle(vehicle: Vehicle){
        _myVehicles.value = _myVehicles.value.filter { it.id != vehicle.id }
    }

    fun getVehicle(vehicleId:String):Vehicle?{
        return _myVehicles.value.firstOrNull { it.id == vehicleId }
    }

    fun setMyVehicles(vehicles: List<Vehicle>){
        _myVehicles.value = vehicles
    }

    fun setMyRides(rides: List<Ride>){
        _myRides.value = rides
    }

    fun setMyRideRequests(rideRequests: List<RideRequest>){
        _myRideRequests.value = rideRequests
    }
    fun setMyRideSubscriptions(subscriptions:List<SubscribedRide>){
        _myRideSubscriptions.value = subscriptions
    }
    fun addRideSubscription(subscribedRide: SubscribedRide){
        _myRideSubscriptions.value += subscribedRide
    }

    fun removeRideSubscription(subscription: Subscription) {
        _myRideSubscriptions.value =
            _myRideSubscriptions.value.filter { it.subscription.id != subscription.id }
    }

    // Refreshes the ride inside a subscribed ride (e.g. after the driver rolled
    // a recurring ride forward), keeping the rider's own subscription intact.
    fun updateSubscribedRide(ride: Ride) {
        _myRideSubscriptions.value = _myRideSubscriptions.value.map {
            if (it.ride.id == ride.id) it.copy(ride = ride) else it
        }
    }

    fun setSession(session: UserSession?) {
        _session.value = session
    }

    fun clearSession() {
        _session.value = null
    }


    fun addRideRequestAccepted(rideRequestId:String){
        Log.d("SESSION", "Removing rideRequest $rideRequestId from available to subscribed")
        val rideRequest = CacheManager.get<RideRequest>(rideRequestId)
        CacheManager.remove<RideRequest>(rideRequestId)
        if (rideRequest==null){
            Log.e("Ride", "RideRequestAccepted null - Cant find in Cache")
            return
        }
        _myRideRequestSubscriptions.value += rideRequest
        Log.d("SESSION", "Removing added $rideRequestId to subscribed")
    }

    fun setAcceptedRideRequests(requests:List<RideRequest>){
        _myRideRequestSubscriptions.value = requests
    }

    fun removeAcceptedRideRequest(rideRequest: RideRequest){
        _myRideRequestSubscriptions.value =
            _myRideRequestSubscriptions.value.filter { it.id != rideRequest.id }
        CacheManager.put<RideRequest>(rideRequest.id, rideRequest)
    }

    public fun saveLoadedArea(location: SimpleLocation, radiusKm: Double) {
        lastLoadedArea = LoadedArea(location, radiusKm)
    }

    public fun isWithinLoadedArea(location: SimpleLocation, radiusKm: Double): Boolean {
        val area = lastLoadedArea ?: return false  // Never loaded before
        val distanceMoved = haversineDistance(area.location, location)
        // Still within range if user hasn't moved outside the originally loaded area
        return distanceMoved + radiusKm <= area.radiusKm
    }

    private fun haversineDistance(a: SimpleLocation, b: SimpleLocation): Double {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val x = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        return 2 * R * asin(sqrt(x))
    }

    // Refreshes an accepted request (e.g. after the rider rolled a recurring
    // request forward) so the driver sees the updated date.
    fun updateAcceptedRideRequest(rideRequest: RideRequest){
        _myRideRequestSubscriptions.value =
            _myRideRequestSubscriptions.value.map { if (it.id == rideRequest.id) rideRequest else it }
    }


    fun updateMyRides(ride: Ride){
        _myRides.value =
            _myRides.value.map { if (it.id == ride.id) ride else it }
    }


 }