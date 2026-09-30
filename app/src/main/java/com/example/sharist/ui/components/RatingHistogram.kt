package com.example.sharist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding

@Composable
fun RatingHistogram(
    ratings: Map<Int, Int>,
    modifier: Modifier = Modifier,
    minStar: Int = 1,
    maxStar: Int = 5
) {
    val total = ratings.values.sum().coerceAtLeast(1)

    Box(
        modifier = modifier
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            for (star in maxStar downTo minStar) {
                RatingBar(
                    star = star,
                    count = ratings[star] ?: 0,
                    total = total
                )

                if (star > minStar) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun RatingBar(star: Int, count: Int, total: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Text(
            text = "$star",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.width(16.dp)
        )

        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            val fraction = count.toFloat() / total

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFFA726))
            )
        }
    }
}