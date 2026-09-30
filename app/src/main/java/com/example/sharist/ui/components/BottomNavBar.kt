package com.example.sharist.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sharist.R
import com.example.sharist.Screen
// -------------------------------------
// NAVIGATION ITEMS
// -------------------------------------

sealed class BottomNavItem(
    val route: String,
    val icon: Int
) {
    object Map : BottomNavItem(Screen.Map.route, R.drawable.map)
    object Schedule : BottomNavItem(Screen.MySubscribedRides.route, R.drawable.scheduled)
    object MyRides : BottomNavItem(Screen.MyRides.route, R.drawable.rides)
    object Locations : BottomNavItem(Screen.MyLocations.route, R.drawable.locations)
}


@Composable
fun BottomNavItemView(
    item: BottomNavItem,
    selected: Boolean,
    onClick: (BottomNavItem) -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick(item) }
            .padding(8.dp)
    ) {

        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = null,
            //tint = if (selected) Color.Blue else Color.Gray,
            tint = Color.Unspecified,
            modifier = Modifier.size(28.dp)
        )

        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .background(Color.Blue)
            )
        }
    }
}

// -------------------------------------
// BOTTOM NAVBAR COMPONENT
// -------------------------------------
@Composable
fun BottomNavBar(
    selected: BottomNavItem,
    onSelected: (BottomNavItem) -> Unit
) {

    val items = listOf(
        BottomNavItem.Map,
        BottomNavItem.Schedule,
        BottomNavItem.MyRides,
        BottomNavItem.Locations
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 50.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 50.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomNavItemView(
                    item = item,
                    selected = selected == item,
                    onClick = onSelected
                )
            }
        }
    }
}