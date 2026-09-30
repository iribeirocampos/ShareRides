package com.example.sharist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.sharist.ui.theme.SharISTTheme
import com.google.android.libraries.places.api.Places
import com.example.sharist.viewmodel.LoginViewModel
import com.example.sharist.ui.screens.SplashScreen
import com.example.sharist.ui.components.events.AppToastHost

class MainActivity : ComponentActivity() {
    private val authViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.MAPS_API_KEY.isNotBlank()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        enableEdgeToEdge()
        setContent {
            SharISTTheme {
                val isChecking by authViewModel.isCheckingSession.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LaunchedEffect(Unit) {
                        authViewModel.checkSession()
                    }

                    if (isChecking) {
                     SplashScreen()
                     } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppNavigation()
                            AppToastHost()
                        }
                }
                }
                }
            }
        }
    }


