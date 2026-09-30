package com.example.sharist.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.ui.components.SubscribedRideCard
import com.example.sharist.viewmodel.MyRideViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.example.sharist.data.model.UserType
import com.example.sharist.ui.components.AcceptedRideRequestCard
import com.example.sharist.ui.components.BottomNavBar
import com.example.sharist.ui.components.BottomNavItem
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.viewmodel.MyRideRequestViewModel


@Composable
fun MySubscribedRidesScreen(
    viewModel: MyRideViewModel = viewModel(),
    requestViewModel: MyRideRequestViewModel = viewModel(),
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit,
    onUserClick: () -> Unit
) {
    val subscriptions by SessionManager.myRideSubscriptions.collectAsState()
    val requestsAccepted by SessionManager.myRideRequestSubscriptions.collectAsState()
    val user by SessionManager.user.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SharistTopBar(isAuthenticated = true, onUserClick = onUserClick)
        },
        bottomBar = {
            BottomNavBar(
                selected = selectedTab,
                onSelected = onTabSelected
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            ScreenTitle(
                showBackButton = false,
                title = "My Subscriptions"
            )

            if (subscriptions.isEmpty() && requestsAccepted.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No subscriptions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    when (user?.type) {
                        UserType.RIDER -> items(subscriptions, key = { it.subscription.id }) { subscription ->
                            SubscribedRideCard(
                                subscription = subscription,
                                onCancelClick = { viewModel.cancelSubscription(subscription) }
                            )
                        }
                        UserType.DRIVER -> items(requestsAccepted, key = { it.id }) { request ->
                            AcceptedRideRequestCard(
                                request = request,
                                onCancelClick = { requestViewModel.cancelAcceptedRequest(context,request) }
                            )
                        }
                        else -> item { ErrorWarning("User Has No Role") }
                    }
                }
            }
        }
    }
}