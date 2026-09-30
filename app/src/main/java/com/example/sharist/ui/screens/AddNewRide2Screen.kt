package com.example.sharist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.R
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.viewmodel.MyRideViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewRide2Screen(
    viewModel: MyRideViewModel = viewModel(),
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val context = LocalContext.current
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
                showBackButton = true,
                onBackClick = onBackClick,
                stringResource(R.string.screen_add_ride2_title)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {

                // Cost Input
                OutlinedTextField(
                    value = uiState.cost.toString(),
                    onValueChange = {
                        viewModel.updateCost(it.toDoubleOrNull() ?: 0.0)
                    },
                    label = {
                        Text("Cost")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Capacity Input
                OutlinedTextField(
                    value = uiState.capacity.toString(),
                    onValueChange = {
                        viewModel.updateCapacity(it.toIntOrNull() ?: 0)
                    },
                    label = {
                        Text("Capacity")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Cancel Days Input
                OutlinedTextField(
                    value = uiState.cancelUpToDays.toString(),
                    onValueChange = {
                        viewModel.updateCancelUpToDays(it.toIntOrNull() ?: 0)
                    },
                    label = {
                        Text("May cancel up to days before")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Weekly repetition checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Checkbox(
                        checked = uiState.isWeekly,
                        onCheckedChange = {
                            viewModel.updateWeeklyRepetition(it)
                        }
                    )

                    Text(
                        text = "Weekly repetition"
                    )
                }

                // Daily repetition checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Checkbox(
                        checked = uiState.isDaily,
                        onCheckedChange = {
                            viewModel.updateDailyRepetition(it)
                        }
                    )

                    Text(
                        text = "Daily repetition"
                    )
                }
                if (uiState.error != null) {
                    ErrorWarning(uiState.error!!)
                }
                Spacer(modifier = Modifier.weight(1f))

                // Add Button
                Button(
                    onClick = {
                        viewModel.addRide(
                            context,
                            onSuccess = {
                                onAddClick()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Add Ride")
                }
            }
        }
    }
}