package com.example.sharist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.R
import com.example.sharist.viewmodel.ChangePasswordViewModel
import com.example.sharist.ui.components.ErrorWarning
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel = viewModel(),
    onBackClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            SharistTopBar(false)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            ScreenTitle(true, onBackClick, stringResource(R.string.screen_password_title))
            Spacer(modifier = Modifier.height(48.dp))
            // New Password

            OutlinedTextField(
                value = uiState.currentPassword,
                label = { Text("Current Password") },
                onValueChange = viewModel::updateCurrentPassword,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            OutlinedTextField(
                value = uiState.password,
                label = { Text("New Password") },
                onValueChange = viewModel::updatePassword,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                value = uiState.confirmedPassword,
                    label = { Text("Confirm Password") },
                onValueChange = viewModel::updateConfirmedPassword,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.weight(1f))
            if (uiState.error!=null){
                ErrorWarning(uiState.error.toString())
            }
            // Change button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                            viewModel.changePassword(
                                onSuccess= {onBackClick()})
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("CHANGE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        LaunchedEffect(uiState.successMessage, uiState.error) {

            uiState.successMessage?.let { message ->
                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearSuccessMessage()
            }


        }
    }
}
