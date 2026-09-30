package com.example.sharist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.R
import com.example.sharist.data.model.UserType
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.viewmodel.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: SignUpViewModel = viewModel(),
    onSignupSuccess: () -> Unit,
    onBackClick:()->Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFormValid =
        uiState.email.isNotBlank() &&
                uiState.password.isNotBlank() &&
                uiState.passwordConfirmation.isNotBlank() &&
                uiState.passwordError == null&&
                uiState.userType != null


    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignupSuccess()
        }
    }
    Scaffold(
        topBar = {
            SharistTopBar(false)
        }

    ){ paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScreenTitle(true,onBackClick, title=stringResource(R.string.screen_signup_title))


            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                    value = uiState.passwordConfirmation,
                    onValueChange = viewModel::updatePasswordConfirmation,
                    label = { Text("Confirm Password") },
                        isError = uiState.passwordError != null,
                        supportingText = {
                            if (uiState.passwordError != null) {
                                Text(
                                    text = uiState.passwordError!!,
                                    color = Color.Red
                                )
                            }
                        },
                    modifier = Modifier.fillMaxWidth()
                    )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    UserType.entries.forEach { type ->
                        val isSelected = uiState.userType == type
                        Button(
                            onClick = { viewModel.updateUserType(type) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color.Blue else Color.LightGray
                            )
                        ) {
                            Text(type.displayName())
                        }
                    }
                }
                if (uiState.error!=null){
                    ErrorWarning(uiState.error.toString())
                }
                    Button(
                        onClick = viewModel::signup,
                        enabled = isFormValid,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Submit",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                   }


        }
    }}

