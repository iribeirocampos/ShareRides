package com.example.sharist.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.R
import com.example.sharist.data.model.UserType
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.BottomNavBar
import com.example.sharist.ui.components.BottomNavItem
import com.example.sharist.ui.components.CurrentLocationMap
import com.example.sharist.ui.components.Legend
import com.example.sharist.viewmodel.MapsViewModel
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import android.util.Log

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapsViewModel = viewModel(),
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit,
    onUserClick: () -> Unit,
    onMarkerClick:(String?, String) -> Unit
) {
    val permissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val windowLocation by viewModel.windowLocation.collectAsState()
    val user by SessionManager.user.collectAsState()
    val availableRides by viewModel.availableRides.collectAsState()
    val availableRideRequests by viewModel.availableRideRequests.collectAsState()

    LaunchedEffect(windowLocation) {
        windowLocation.let { location ->
            Log.d("LOCATION", "USerType ${user?.type}")
               when (user?.type) {
                UserType.RIDER -> viewModel.refreshRides(location)
                UserType.DRIVER -> viewModel.refreshRideRequests(location)
                else -> {
                    Log.d("LOCATION", "Did nothing, no user")
                }
            }
        }
    }


    val markers =
        when (user?.type) {
            UserType.DRIVER -> {
                viewModel.buildRideRequestsMarkers(availableRideRequests)
            }
            UserType.RIDER -> {
                viewModel.buildRideMarkers(availableRides)
            }
            else -> emptyList()
        }

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            SharistTopBar(true, onUserClick)
        },
        bottomBar = {
            BottomNavBar(
                selected = selectedTab,
                onSelected = onTabSelected
            )
        }

    ){ paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScreenTitle(false, title=stringResource(R.string.screen_map_title))

                if (permissionState.status.isGranted) {
                    CurrentLocationMap(viewModel.getMyLocations(), markers, onMarkerClick, onCameraMove = { viewModel.updateCameraLocation(it) } )
                } else {
                    Text("Location permission required")
                }
            Spacer(modifier = Modifier.weight(1f))
            Legend(user?.type.toString())
            }
        }

    }

