package com.example.sharist.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.ui.components.ErrorWarning
import com.example.sharist.ui.components.RatingSelector
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.viewmodel.MakeReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeReviewScreen(
    userId:String,
    viewModel: MakeReviewViewModel = viewModel(),
    onBackClick: () -> Unit,
    onUserClick: () -> Unit,
    onConfirmClick:()->Unit
) {

    LaunchedEffect(userId) { viewModel.setUsersId(userId) }
    val newReviewUiState by viewModel.newReview.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SharistTopBar(isAuthenticated = true, onUserClick = onUserClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            ScreenTitle(
                showBackButton = true,
                onBackClick = onBackClick,
                title = "Make Review"
            )

            Text(
                text = "Overall Rating",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            RatingSelector(
                selected = newReviewUiState.rating,
                onSelect = viewModel::updateRating,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Comments:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newReviewUiState.comment,
                onValueChange = viewModel::updateComment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("Write your comment...") }
            )
            if(newReviewUiState.error!=""){
                ErrorWarning(newReviewUiState.error)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.addReview(context, onConfirmClick)
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("CONFIRM", color = Color.Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
