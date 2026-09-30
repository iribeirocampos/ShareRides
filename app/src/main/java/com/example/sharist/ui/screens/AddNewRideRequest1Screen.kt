package com.example.sharist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.DateTimePickerField
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.ui.components.LocationAutocompleteField
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.viewmodel.MyRideRequestViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewRideRequest1Screen(
    viewModel: MyRideRequestViewModel = viewModel(),
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onUserClick: () -> Unit
) {

    val uiState by viewModel.requestUiState.collectAsState()

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
                "Add Ride Request 1/2"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {

                // DEPARTURE LOCATION
                LocationAutocompleteField(
                    label = "Initial Location",
                    onLocationSelected = { location ->
                        viewModel.updateRequestInitialLocation(location)
                    }
                )

                // DATE & TIME
                DateTimePickerField(
                    label = "Departure Date & Time",
                    value = uiState.date,
                    onValueChange = viewModel::updateRequestDate
                )

                // DESTINATION LOCATION
                LocationAutocompleteField(
                    label = "Destination Location",
                    onLocationSelected = { location ->
                        viewModel.updateRequestDestination(location)
                    }
                )

                // TOLERANCE RANGE
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "Tolerance Range (meters)",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.toleranceRange.toString(),
                        onValueChange = {
                            viewModel.updateTolerance(
                                it.toIntOrNull() ?: 0
                            )
                        },
                        label = {
                            Text("Departure Tolerance")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ERROR
                if (uiState.error != null) {
                    ErrorWarning(uiState.error!!)
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                // NEXT BUTTON
                Button(
                    onClick = {
                        viewModel.nextRequest1 {
                            onNextClick()
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