package com.example.sharist.ui.components

import android.R.attr.navigationIcon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sharist.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharistTopBar(
    isAuthenticated: Boolean,
    onUserClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SharIST",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Blue
                )
            }
        },

        actions = {
            if (isAuthenticated) {
                IconButton(onClick = onUserClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "User Profile",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                // keeps spacing consistent so title doesn't shift
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    )
}