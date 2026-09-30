package com.example.sharist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.data.local.util.formatDateTime
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.ui.components.UserDetailsCard
import com.example.sharist.viewmodel.MyRideRequestViewModel
import com.example.sharist.viewmodel.MyRideViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideRequestDetailScreen(
    requestId: String,
    viewModel: MyRideRequestViewModel = viewModel(),
    onBackClick: () -> Unit,
    onUserClick: () -> Unit,
    onRiderClick:(String)->Unit,
    onAcceptClick:()->Unit
) {
    val request by viewModel.currentRideRequest.collectAsState()
    val context = LocalContext.current
    val rider by viewModel.targetProfile.collectAsState()

    LaunchedEffect(request?.userId) {
        request?.userId?.let { viewModel.loadTargetProfile(it) }
        viewModel.loadRideRequest(requestId)
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
                        title = "Ride Request Details"
                    )

                    if (request == null || rider==null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Ride Request not found", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val request = request!!
                            UserDetailsCard(rider!!,onClick = { onRiderClick(request.userId) } )
                            // Rota do Pedido
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Requested Route", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("From: ${request.departure.address}", style = MaterialTheme.typography.bodyLarge)
                                    Text("To: ${request.destination.address}", style = MaterialTheme.typography.bodyLarge)
                                }
                            }

                            //time window n tolerence
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Target Date & Flexibility", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Desired Date: ${formatDateTime(request.date)}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Distance Tolerance: ${request.toleranceRange} meters", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Recurrence
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Recurrence", style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• Weekly Repetition: ${if(request.isWeekly) "Yes" else "No"}", style = MaterialTheme.typography.bodyMedium)
                                    Text("• Daily Repetition: ${if(request.isDaily) "Yes" else "No"}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Button to respond or subscribe
                            Button(
                                onClick = { viewModel.acceptRequest(context,request, SessionManager.user.value!!.id, onAcceptClick) },
                                        modifier = Modifier
                                        .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Offer a Ride for this Request", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }}

            }
}