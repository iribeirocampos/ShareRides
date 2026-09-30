package com.example.sharist.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box


@Composable
fun Legend(role:String) {
    var legend: String =""
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        LegendItem(
            text = "My Locations",
            color = Color(0xFF1E88E5) // blue
        )

        Spacer(modifier = Modifier.height(8.dp))
        if (role=="DRIVER"){
            legend  ="Available Ride Requests"
        }
        if (role=="RIDER"){
            legend ="Available Rides"
        }
        LegendItem(
            text = legend,
            color = Color(0xFF43A047) // green
        )
    }
}

@Composable
fun LegendItem(
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = text)
    }
}