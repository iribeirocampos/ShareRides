package com.example.sharist.ui.components.events


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AppToastHost() {

    var currentEvent by remember {
        mutableStateOf<UiEvent?>(null)
    }

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        UiEventBus.events.collect { event ->

            currentEvent = null
            visible = false

            currentEvent = event
            visible = true

            delay(3000)

            visible = false
            delay(300)

            currentEvent = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {

            val event = currentEvent ?: return@AnimatedVisibility

            val background = when (event) {
                is UiEvent.Success -> Color(0xFF2E7D32)
                is UiEvent.Error -> Color(0xFFC62828)
                is UiEvent.Warning -> Color(0xFFF9A825)
                is UiEvent.Info -> Color(0xFF1565C0)
            }

            val message = when (event) {
                is UiEvent.Success -> event.message
                is UiEvent.Error -> event.message
                is UiEvent.Warning -> event.message
                is UiEvent.Info -> event.message
            }

            Box(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth(0.9f)
                    .background(background, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}