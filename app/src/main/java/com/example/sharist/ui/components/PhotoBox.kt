package com.example.sharist.ui.components

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
public fun PhotoBox(
    photoUrl:String?,
    localPhotoPath:String?=null,
    onClick: () -> Unit = {}
) {
    val model =
        if (!localPhotoPath.isNullOrEmpty()) {
            val file = File(localPhotoPath) // Local Cache, instead of downloading from supabase
            if (file.exists()) {
                Log.d("IMAGE", "Loading from local")
                file
            }else {
                Log.d("IMAGE", "Loading from supabase")
                photoUrl
            }
        } else {
            Log.d("IMAGE", "Loading from supabase")
            photoUrl
        }

    // User photo
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onClick() }, // <- click handler
        contentAlignment = Alignment.Center
    ) {
        if (model!=null) {
            AsyncImage(
                model = model,
                contentDescription = "Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "No photo",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
        }
    }
}