package com.example.sharist.ui.screens

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.ui.components.DateTimePickerField
import com.example.sharist.ui.components.LocationAutocompleteField
import com.example.sharist.viewmodel.MyRideViewModel




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewRide1Screen(
    viewModel: MyRideViewModel = viewModel(),
    onBackClick: () -> Unit,
    onNext1Click: () -> Unit,
    onUserClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SharistTopBar(true, onUserClick)
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            ScreenTitle(
                true,
                onBackClick,
                "Add New Ride"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {

                // Initial Location
                LocationAutocompleteField(
                    label = "Initial Location",
                    onLocationSelected = { location ->
                        viewModel.updateInitialLocation(location)
                    }
                )
                // TIME PICKER
                DateTimePickerField(
                    label = "Departure Date & Time",
                    value = uiState.departureDateTime,
                    onValueChange = viewModel::updateDepartureDateTime
                )

                LocationAutocompleteField(
                    label = "Arrival Location",
                    onLocationSelected = { location ->
                        viewModel.updateDestinationLocation(location)
                    }
                )
                // TIME PICKER
                DateTimePickerField(
                    label = "Arrival Date & Time",
                    value = uiState.arrivalDateTime,
                    onValueChange = viewModel::updateArrivalDateTime
                )

                // Error
                if (uiState.error != null) {
                    ErrorWarning(uiState.error!!)
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                // Next Button
                Button(
                    onClick = {
                        viewModel.next1 {
                            onNext1Click()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Next")
                }
            }
        }

    }
}

