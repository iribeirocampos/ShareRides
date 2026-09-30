package com.example.sharist.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.SimpleLocation
import com.example.sharist.viewmodel.MapMarker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID



@Composable
fun CurrentLocationMap(
    myLocations: StateFlow<List<Location>>,
    markers: List<MapMarker>,
    onMarkerClick:(String?, String)->Unit,
    onCameraMove: (SimpleLocation) -> Unit
    ) {
    val coroutineScope = rememberCoroutineScope()
    val locations by myLocations.collectAsState()
    val context = LocalContext.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val fallbackLocation = LatLng(
        38.739993, // Taguspark
        -9.303020
    )

    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val currentLocation = remember { mutableStateOf<LatLng?>(null) }
    val mapLocation = currentLocation.value ?: fallbackLocation
    LaunchedEffect(hasPermission.value) {
        if (hasPermission.value) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation.value = LatLng(
                        location.latitude,
                        location.longitude
                    )
                }
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(mapLocation) {
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(mapLocation, 15f) // aprox 1 Km
        }
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val center = cameraPositionState.position.target
            onCameraMove(
                SimpleLocation(
                    id = UUID.randomUUID().toString(),
                    address = "",
                    latitude = center.latitude,
                    longitude = center.longitude
                )
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(12.dp)
    ) {

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(12.dp),
            cameraPositionState = cameraPositionState
        ) {


            // My locations (blue markers)
            locations.forEach { loc ->
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            loc.latitude,
                            loc.longitude
                        )
                    ),
                    title = loc.address,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
            markers.forEach { marker ->

                Marker(
                    state = MarkerState(
                        position = LatLng(marker.lat, marker.lng)
                    ),
                    title = marker.title,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        marker.color
                    ),
                    onClick = {
                        onMarkerClick(marker.detailId, marker.type)
                        Log.d("MARKER", "Clicked in ${marker.detailId}")
                        true
                    }
                )
            }
        }

        // 🔍 SEARCH BAR (THIS WAS MISSING)
        LocationAutocompleteField(
            label = "Search address",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            onLocationSelected = { location ->

                coroutineScope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            15f
                        )
                    )
                }
            }
        )
    // 2. FLOATING BUTTON (overlay on top of map)
    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end=80.dp,bottom=16.dp),
        onClick = {
            currentLocation.value?.let { location ->
                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(location, 15f)
            }
        }
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "Go to current location"
        )
    }
    }

}