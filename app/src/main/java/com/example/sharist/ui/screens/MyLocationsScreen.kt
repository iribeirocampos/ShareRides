package com.example.sharist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.viewmodel.MyLocationsViewModel
import com.example.sharist.ui.components.LocationCard
import androidx.core.net.toUri
import com.example.sharist.ui.components.BottomNavBar
import com.example.sharist.ui.components.BottomNavItem
@Composable
fun MyLocationsScreen(
    viewModel: MyLocationsViewModel = viewModel(),
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit,
    onNewLocationClick: () -> Unit,
    onLocationClick: (String) -> Unit = {},
    onUserClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val locations by viewModel.getLocations().collectAsState()
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
                title = "My Locations"
            )

            if (locations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "No locations yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(locations, key = { it.id }) { location ->
                        LocationCard(
                            location = location,
                            photoUri = (location.localPhotoUrl ?: location.remotePhotoUrl)?.toUri(),
                            onClick = { onLocationClick(location.id) },
                            onDelete = { viewModel.removeLocation(context, location) }
                        )
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
                    onClick = onNewLocationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("New Location", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}