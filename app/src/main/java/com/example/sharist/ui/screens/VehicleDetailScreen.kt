package com.example.sharist.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
fun VehicleDetailScreen(
    vehicleId: String,
    viewModel: MyVehiclesViewModel=viewModel(),
    onBackClick: () -> Unit,
        onUserClick: () -> Unit
) {
    val vehicle = viewModel.getVehicle(vehicleId)
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.updateUri(selectedUri)
        }
    }

    Scaffold(
        topBar = { SharistTopBar(true, onUserClick,) }
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
                title = stringResource(R.string.screen_vehicle_detail_title)
            )

            if (vehicle == null) {
                Text("Vehicle not found.")
                return@Scaffold
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Capacity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = vehicle.capacity.toString(),
                            onValueChange = {},
                            modifier = Modifier.width(80.dp),
                            readOnly = true,
                            singleLine = true,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                PhotoBox(vehicle.remotePhotoUrl, vehicle.localPhotoUrl, onClick={
                    photoPickerLauncher.launch("image/*")
                })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Licence Plate",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = vehicle.licencePlate,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
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
                value = vehicle.brand,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
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
                value = vehicle.model,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))



            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        viewModel.removeVehicle(context,vehicle)
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("DELETE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
