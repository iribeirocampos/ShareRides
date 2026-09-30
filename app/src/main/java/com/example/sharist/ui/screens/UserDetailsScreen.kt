package com.example.sharist.ui.screens

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharist.data.model.toStats
import com.example.sharist.ui.components.PhotoBox
import com.example.sharist.ui.components.RatingHistogram
import com.example.sharist.ui.components.ReviewCard
import com.example.sharist.ui.components.ScreenTitle
import com.example.sharist.ui.components.SharistTopBar
import com.example.sharist.viewmodel.UserDetailsViewModel
import androidx.compose.ui.platform.LocalConfiguration
import com.example.sharist.ui.components.UserVehicleRow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(
    userId: String,
    viewModel: UserDetailsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onUserClick: () -> Unit,
    onMakeReviewClick: (String) -> Unit
) {
    LaunchedEffect(userId) { viewModel.load(userId) }

    val profile by viewModel.profile.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val stats = remember(reviews) {
        reviews.toStats()
    }
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

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
                title = "Rating"
            )

            // Rating + photo row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rating",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val ratingText = if (stats.average > 0f)
                            String.format(locale, "%.1f", stats.average)
                        else "0"
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
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RatingHistogram(
                        ratings = stats.histogram,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                PhotoBox(profile?.photoUrl, profile?.localPhotoPath)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Name: ${profile?.username ?: "..."}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type: ${profile?.type?.displayName() ?: "..."}",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicles
            Text(
                text = "Vehicle",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            vehicles.firstOrNull()?.let { vehicle ->
                UserVehicleRow(vehicle)
            } ?: Text(
                "No vehicles",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Ratings / Reviews
            Text(
                text = "Ratings/Reviews",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reviews.forEach { review ->
                    ReviewCard(review)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onMakeReviewClick(userId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Make Review", color = Color.Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
