package com.example.sharist.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.R
import com.example.sharist.ui.components.PhotoBox
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.viewmodel.MyVehiclesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewVehicleScreen(
    viewModel: MyVehiclesViewModel=viewModel(),
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.updateUri(selectedUri)
        }
    }

    Scaffold(
        topBar = { SharistTopBar(true, onUserClick) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            ScreenTitle(
                showBackButton = true,
                onBackClick = onBackClick,
                title = stringResource(R.string.screen_add_vehicle_title)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Capacity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = uiState.capacity.toString(),
                            onValueChange = viewModel::updateCapacity,
                            modifier = Modifier.width(80.dp),
                            singleLine = true,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Color",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = uiState.color,
                            onValueChange = viewModel::updateColor,
                            modifier = Modifier.width(120.dp),
                            singleLine = true,
                        )
                    }

                    PhotoBox(uiState.photoUri?.toString(), onClick = {
                        photoPickerLauncher.launch("image/*")
                    })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Licence Plate",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = uiState.licencePlate,
                onValueChange = viewModel::updateLicencePlate,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Brand",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = uiState.brand,
                onValueChange = viewModel::updateBrand,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Model",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = uiState.model,
                onValueChange = viewModel::updateModel,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.weight(1f))

            uiState.error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        viewModel.addVehicle(context,
                            onSuccess = {
                                onAddClick()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("ADD", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
