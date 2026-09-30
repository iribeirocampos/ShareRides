package com.example.sharist.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.ui.components.PhotoBox
import com.example.sharist.R
import com.example.sharist.data.model.UserType
import com.example.sharist.data.model.toStats
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.RatingHistogram
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.viewmodel.UserProfileViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: UserProfileViewModel = viewModel(),
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit = {},
    onMyVehiclesClick: ()-> Unit={},
    onLogOffClick:()->Unit
) {
    val profile by SessionManager.user.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.updatePhotoUri(context, selectedUri)
        }
    }

    Scaffold(
        topBar = {
            SharistTopBar(false)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            ScreenTitle(true, onBackClick, stringResource(R.string.screen_settings_title))
            // Back arrow + Rating row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rating",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (profile == null) {
                    Text("Loading...")
                }else{
                    val ratingText = reviews.toStats().average
                        .takeIf { it > 0 }
                        ?.let { String.format(Locale.getDefault(), "%.0f", it) }
                        ?: "0"
                Text(
                    text = ratingText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Star",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )}
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Histogram + Photo row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                RatingHistogram(ratings = reviews.toStats().histogram, modifier = Modifier.weight(1f) )
                Spacer(modifier = Modifier.width(16.dp))
                 PhotoBox(profile?.photoUrl, profile?.localPhotoPath, onClick = {
                    photoPickerLauncher.launch("image/*")
                })
            }

            Spacer(modifier = Modifier.height(16.dp))


            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("NAME") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email field
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("EMAIL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.error != null) {
                ErrorWarning(uiState.error.toString())
            }

            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Save", color = Color.Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // My Vehicles button //
            if (profile?.type==UserType.DRIVER){
                Button(
                onClick = onMyVehiclesClick ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("My Vehicles", color = Color.Black, fontSize = 16.sp)
            }
            }
            Button(
                onClick ={ viewModel.logOff(context,
                    onSuccess={
                        onLogOffClick()
                    }
                )},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Log Off", color = Color.Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Change Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onChangePasswordClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Change Password", color = Color.Black)
                }

            }

        }
    }
}
