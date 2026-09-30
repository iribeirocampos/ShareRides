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
import com.example.sharist.viewmodel.MyRideRequestViewModel
import com.example.sharist.viewmodel.MyRideViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewRideRequest2Screen(
    viewModel: MyRideRequestViewModel = viewModel(),
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val context = LocalContext.current
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
                showBackButton = true,
                onBackClick = onBackClick,
                title = "New Ride Request 2/2 "
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // WEEKLY REPETITION
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Checkbox(
                        checked = uiState.isWeekly,
                        onCheckedChange = {
                            viewModel.updateWeekly(it)
                        }
                    )

                    Text(
                        text = "Weekly repetition"
                    )
                }

                // DAILY REPETITION
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Checkbox(
                        checked = uiState.isDaily,
                        onCheckedChange = {
                            viewModel.updateDaily(it)
                        }
                    )

                    Text(
                        text = "Daily repetition"
                    )
                }

                // ERROR
                if (uiState.error != null) {
                    ErrorWarning(uiState.error!!)
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                // ADD BUTTON
                Button(
                    onClick = {
                        viewModel.addRideRequest(
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
                    Text("Add Ride Request")
                }
            }
        }
    }
}