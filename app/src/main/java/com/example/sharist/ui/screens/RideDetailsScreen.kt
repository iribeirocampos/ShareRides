package com.example.sharist.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.data.local.util.formatDateTime
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.viewmodel.MyRideViewModel
import com.example.sharist.ui.components.UserDetailsCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailScreen(
    rideId: String,
    viewModel: MyRideViewModel = viewModel(),
    onBackClick: () -> Unit,
    onUserClick: () -> Unit,
    onDriverClick: (String) -> Unit,
    onSubscribeClick:()->Unit
) {
    val ride by viewModel.currentRide.collectAsState()
    val context = LocalContext.current
    val driver by viewModel.targetProfile.collectAsState()

    LaunchedEffect(rideId) {
        viewModel.loadRide(rideId)
    }
    LaunchedEffect(ride?.userId) {
        ride?.userId?.let { viewModel.loadTargetProfile(it) }
    }
    Scaffold(
                topBar = {
                    SharistTopBar(isAuthenticated = true, onUserClick = onUserClick)
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ScreenTitle(
                        showBackButton = true,
                        onBackClick = onBackClick,
                        title = "Ride Details"
                    )

                    if (ride == null || driver==null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Ride not found", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        val ride = ride!!
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                          UserDetailsCard(driver!!,onClick = { onDriverClick(ride.userId) } )
                            // route
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Route Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("From: ${ride.departure.address}", style = MaterialTheme.typography.bodyLarge)
                                    Text("To: ${ride.destination.address}", style = MaterialTheme.typography.bodyLarge)
                                }
                            }

                            // schedules
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Schedule", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Departure: ${formatDateTime(ride.departureDateTime)}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Arrival: ${formatDateTime(ride.arrivalDateTime)}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            //price n places
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Cost", style = MaterialTheme.typography.labelMedium)
                                        Text("${ride.cost} €", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Available Seats", style = MaterialTheme.typography.labelMedium)
                                        Text("${ride.availableSeats}", style = MaterialTheme.typography.headlineSmall)
                                    }
                                }
                            }


                            Spacer(modifier = Modifier.height(16.dp))

                            //subscribe button
                            Button(
                                onClick = { viewModel.subscribeRide(context, ride.id, false, onSubscribeClick) },
                                        modifier = Modifier
                                        .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Subscribe to Ride", style = MaterialTheme.typography.titleMedium)
                            }
                            Button(
                                onClick = { viewModel.subscribeRide(context, ride.id, true, onSubscribeClick) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Subscribe If Rains", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
}
