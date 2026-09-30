package com.example.sharist.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.viewmodel.LoginViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
           SharistTopBar(false, )
        }
    ){ paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
               .fillMaxSize()
               .padding(paddingValues)

        ) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange =viewModel::updateEmail,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
            )
            if (uiState.error!=null){
                ErrorWarning(uiState.error.toString())
            }
            Button(
                onClick = {
                        viewModel.login(
                            onSuccess = {
                                onLoginClick()
                            }
                        )
                          },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                  Text(
                    text = "Submit",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "If you don't have an account, please ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Signup",
                    fontSize = 12.sp,
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onSignupClick()
                    }
                )
            }
            }
        }
    }
