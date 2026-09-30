package com.example.sharist.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import com.example.sharist.viewmodel.MyLocationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    locationId: String,
    viewModel: MyLocationsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val location = viewModel.get(locationId)
    val context = LocalContext.current

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
                title = stringResource(R.string.screen_location_detail_title)
            )

            if (location == null) {
                Text("Location not found.")
                return@Scaffold
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    PhotoBox(location.remotePhotoUrl, location.localPhotoUrl)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Address",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = location.address,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tag",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = location.name,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        viewModel.removeLocation(context, location)
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
