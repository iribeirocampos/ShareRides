package com.example.sharist.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.RideRequest
import com.example.sharist.data.model.SimpleLocation
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.LocationRepository
import com.example.sharist.data.repository.RideRepository
import com.example.sharist.data.repository.RideRequestRepository
import com.example.sharist.sync.ValidationManager
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID


data class MapMarker(
    val lat: Double,
    val lng: Double,
    val title: String,
    val color: Float,
    val type: String = "location",
    val detailId:String?=null

)
class MapsViewModel (application: Application): AndroidViewModel(application) {

    private val db = SharistDatabase.getDatabase(application)
    private val locationRepository = LocationRepository(
        dao =  db.locationDao(),
        remote = StorageRemoteDataSource(),
        local = FileStorageDataSource()
    )
    private val ridesRepository = RideRepository(
        dao =  db.rideDao(),
        remote = StorageRemoteDataSource(),
    )
    private val requestsRepository = RideRequestRepository(
        dao = db.rideRequestDao(),
        remote = StorageRemoteDataSource(),
    )

    private val _windowLocation = MutableStateFlow<SimpleLocation>(SimpleLocation(UUID.randomUUID().toString(),"Taguspark",38.739993,-9.303020 ))
    val windowLocation: StateFlow<SimpleLocation> = _windowLocation
    private val _availableRides = MutableStateFlow(emptyList<Ride>())
    val availableRides = _availableRides.asStateFlow()
    private val _availableRideRequests = MutableStateFlow(emptyList<RideRequest>())
    val availableRideRequests = _availableRideRequests.asStateFlow()

    fun updateCameraLocation(location: SimpleLocation) {
        Log.d("LOCATION", "Location changed updating Camera")
        _windowLocation.value = location
    }


    fun getMyLocations() : StateFlow<List<Location>> {
    return locationRepository.getLocations()
    }

    fun buildRideMarkers(rides:List<Ride>): List<MapMarker> {
        Log.d("MARKER", "building Rides markers $rides")
        return rides.map { ride ->
            MapMarker(
                lat = ride.departure.latitude,
                lng = ride.departure.longitude,
                title = "${ride.departure.address} → ${ride.destination.address}",
                color = BitmapDescriptorFactory.HUE_GREEN,
                type="ride",
                detailId = ride.id
            )
        }
    }
    fun buildRideRequestsMarkers(rideRequests:List<RideRequest>):List<MapMarker>{
        Log.d("MARKER", "building RideRequest markers $rideRequests")
        return rideRequests.map { ride ->
            MapMarker(
                lat = ride.departure.latitude,
                lng = ride.departure.longitude,
                title = "${ride.departure.address} → ${ride.destination.address}",
                color = BitmapDescriptorFactory.HUE_GREEN,
                type="rideRequest",
                detailId = ride.id
            )
        }
    }

    fun refreshRides(location:SimpleLocation){
        Log.d("LOCATION", "Trying to refresh Rides")
        viewModelScope.launch{
            _availableRides.value = ridesRepository.loadAvailableRides(getApplication(),location)
        }

    }

    fun refreshRideRequests(location:SimpleLocation){
        Log.d("LOCATION", "Trying to refresh Ride requests")
        viewModelScope.launch{
            _availableRideRequests.value = requestsRepository.loadAvailableRideRequests(getApplication(),location)
        }
    }
}