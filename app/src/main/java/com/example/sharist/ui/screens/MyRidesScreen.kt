package com.example.sharist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.R
import com.example.sharist.data.model.UserType
import com.example.sharist.ui.components.BottomNavBar
import com.example.sharist.ui.components.BottomNavItem
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.RideCard
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.example.sharist.ui.components.RideRequestCard
import com.example.sharist.viewmodel.MyRideViewModel
import com.example.sharist.viewmodel.MyRideRequestViewModel
@Composable
fun MyRidesScreen(
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit,
    onNewRideClick: () -> Unit,
    onNewRideRequestClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val user by SessionManager.user.collectAsState()

    Scaffold(
        topBar = { SharistTopBar(true, onUserClick) },
        bottomBar = {
            BottomNavBar(selected = selectedTab, onSelected = onTabSelected)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScreenTitle(false, title = stringResource(R.string.screen_my_rides_title))

            when (user?.type) {
                UserType.DRIVER -> DriverContent(onNewRideClick = onNewRideClick)
                UserType.RIDER -> RiderContent(onNewRideRequestClick = onNewRideRequestClick)
                else -> ErrorWarning("User Has No Role")
            }
        }
    }
}

@Composable
fun DriverContent(
    viewModel: MyRideViewModel = viewModel(),
    onNewRideClick: () -> Unit
) {
    val context = LocalContext.current
    val rides by viewModel.myRides.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(rides, key = { it.id }) { ride ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RideCard(
                        ride = ride,
                        onDelete = { viewModel.removeRide(context, ride) }
                    )
                    if (ride.isWeekly || ride.isDaily) {
                    TextButton(onClick = { viewModel.stopRecurringRide(context, ride) }) {
                        Text("Stop recurring")
                }
                }
            }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onNewRideClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("New Ride", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun RiderContent(
    viewModel: MyRideRequestViewModel = viewModel(),
    onNewRideRequestClick: () -> Unit
) {
    val context = LocalContext.current
    val rides by viewModel.myRideRequests.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(rides, key = { it.id }) { rideRequest ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RideRequestCard(
                        rideRequest = rideRequest,
                        onDelete = { viewModel.removeRideRequest(context, rideRequest) }
                    )
                    // Soft-stop a recurring request: it stops rolling forward and
                    // ages out, without deleting it like onDelete does.
                    if ((rideRequest.isWeekly || rideRequest.isDaily) && rideRequest.active) {
                        TextButton(onClick = { viewModel.stopRecurringRequest(context, rideRequest) }) {
                            Text("Stop recurring")
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onNewRideRequestClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("New Ride Request", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}