package com.example.sharist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sharist.ui.screens.AddNewRide1Screen
import com.example.sharist.ui.screens.LoginScreen
import com.example.sharist.ui.screens.MapScreen
import com.example.sharist.ui.screens.AddNewLocationScreen
import com.example.sharist.ui.screens.AddNewVehicleScreen
import com.example.sharist.ui.screens.LocationDetailScreen
import com.example.sharist.ui.screens.MyLocationsScreen
import com.example.sharist.ui.screens.ChangePasswordScreen
import com.example.sharist.ui.screens.SettingsScreen
import com.example.sharist.ui.screens.SignupScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.BottomNavItem
import com.example.sharist.ui.screens.AddNewRide2Screen
import com.example.sharist.ui.screens.AddNewRideRequest1Screen
import com.example.sharist.ui.screens.AddNewRideRequest2Screen
import com.example.sharist.ui.screens.MyRidesScreen
import com.example.sharist.ui.screens.MySubscribedRidesScreen
import com.example.sharist.ui.screens.MyVehiclesScreen
import com.example.sharist.ui.screens.RideDetailScreen
import com.example.sharist.ui.screens.RideRequestDetailScreen
import com.example.sharist.ui.screens.UserDetailsScreen
import com.example.sharist.ui.screens.MakeReviewScreen
import com.example.sharist.ui.screens.VehicleDetailScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    //object Main : Screen("main")
    object Signup: Screen("signup")
    object Settings : Screen("settings")
    object AddNewRide1: Screen("newride1")
    object AddNewRide2: Screen("newride2")
    object AddRequestRide1 : Screen("requestride1")
    object AddRequestRide2 : Screen("requestride2")
    object Map: Screen("map")
    object ChangePassword: Screen("changepassword")
    object MyLocations: Screen("mylocations")
    object AddNewLocation: Screen("addnewlocation")
    object LocationDetail: Screen("locationdetail/{locationId}") {
        fun createRoute(locationId: String) = "locationdetail/$locationId"
    }
    object MyVehicles: Screen("myvehicles")
    object AddNewVehicle: Screen("addnewvehicle")
    object VehicleDetail: Screen("vehicledetail/{vehicleId}") {
        fun createRoute(vehicleId: String) = "vehicledetail/$vehicleId"
    }
    object MyRides:Screen("myrides")
    object MySubscribedRides:Screen("mysubscribedrides")
    object RideDetails:Screen("ridedetails/{rideId}"){
        fun createRoute(rideId: String) = "ridedetails/$rideId"
    }
    object RideRequestDetails:Screen("riderequestdetails/{rideRequestId}"){
        fun createRoute(rideRequestId: String) = "riderequestdetails/$rideRequestId"
    }
    object UserDetails:Screen("userdetails/{userId}"){
        fun createRoute(userId: String) = "userdetails/$userId"
    }
    object MakeReview:Screen("makereview/{userId}"){
        fun createRoute(userId: String) = "makereview/$userId"
    }
}

fun resolveBottomDestination(
    item: BottomNavItem
): String {
    return when (item) {

        BottomNavItem.Map -> Screen.Map.route

        BottomNavItem.Schedule -> Screen.MySubscribedRides.route

        BottomNavItem.MyRides -> Screen.MyRides.route

        BottomNavItem.Locations -> Screen.MyLocations.route
    }
}
@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val isLoggedIn by SessionManager.isLoggedIn.collectAsState()

    val startDestination = if (isLoggedIn) {
        Screen.Map.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Map.route)
                },
                onSignupClick = {
                    navController.navigate(Screen.Signup.route)
                },

            )
        }
        composable ( Screen.Signup.route ){
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }},
                    onBackClick={navController.popBackStack()}
            )
        }
        composable ( Screen.Settings.route ){
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onChangePasswordClick = {
                    navController.navigate(Screen.ChangePassword.route)
                },
                onMyVehiclesClick = {
                    navController.navigate(Screen.MyVehicles.route)
                },
                onLogOffClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
        composable(Screen.MyVehicles.route) {
            MyVehiclesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onVehicleClick = { vehicleId ->
                    navController.navigate(
                        Screen.VehicleDetail.createRoute(vehicleId)
                    )
                },
                onNewVehicleClick = {
                    navController.navigate(Screen.AddNewVehicle.route)
                },
            )
        }
        composable ( Screen.AddNewVehicle.route ){
            AddNewVehicleScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAddClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable (
            route = Screen.VehicleDetail.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ){  backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: return@composable
            VehicleDetailScreen(
                vehicleId = vehicleId,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }

        composable ( Screen.MyLocations.route ){
            MyLocationsScreen(
                onNewLocationClick = {
                    navController.navigate(Screen.AddNewLocation.route)
                },
                onLocationClick = { locationId ->
                    navController.navigate(Screen.LocationDetail.createRoute(locationId))
                },
                selectedTab = BottomNavItem.Locations,
                onTabSelected = {
                        item ->
                    navController.navigate(resolveBottomDestination(item))
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable (
            route = Screen.LocationDetail.route,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ){  backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable
            LocationDetailScreen(
                locationId = locationId,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable ( Screen.AddNewLocation.route ){
            AddNewLocationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable ( Screen.ChangePassword.route ){
            ChangePasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable ( Screen.AddNewRide1.route ){
            AddNewRide1Screen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNext1Click={
                    navController.navigate(Screen.AddNewRide2.route)
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }

        composable ( Screen.AddNewRide2.route ){
            AddNewRide2Screen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAddClick = {
                    navController.navigate(Screen.MyRides.route) {

                        popUpTo(Screen.MyRides.route) {
                            inclusive = false
                        }

                        launchSingleTop = true
                    }
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
        composable ( Screen.AddRequestRide1.route ){
            AddNewRideRequest1Screen(
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onNextClick = {
                    navController.navigate(Screen.AddRequestRide2.route)
                }
            )
        }
        composable ( Screen.AddRequestRide2.route ){
            AddNewRideRequest2Screen(
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onAddClick = {
                    navController.navigate(Screen.MyRides.route) {
                        popUpTo(Screen.MyRides.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
              )
        }
        composable (route= Screen.Map.route){
            MapScreen(
                selectedTab = BottomNavItem.Map,
                onTabSelected = {
                        item ->
                    navController.navigate(resolveBottomDestination(item))
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onMarkerClick = {id, type ->
                    when (type) {
                        "ride" -> {
                            navController.navigate(Screen.RideDetails.createRoute(id!!))
                        }

                        "rideRequest" -> {
                            navController.navigate(Screen.RideRequestDetails.createRoute(id!!))
                        }
                }
                }
            )
        }
        composable (
            route = Screen.RideDetails.route,
            arguments = listOf(navArgument("rideId") { type = NavType.StringType })
        ){  backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId") ?: return@composable
            RideDetailScreen(
                rideId = rideId,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onDriverClick = { driverId ->
                    navController.navigate(Screen.UserDetails.createRoute(driverId))
                },
                onSubscribeClick = {
                    navController.navigate(Screen.MySubscribedRides.route) {
                        popUpTo(Screen.MySubscribedRides.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable (
            route = Screen.RideRequestDetails.route,
            arguments = listOf(navArgument("rideRequestId") { type = NavType.StringType })
        ){  backStackEntry ->
            val rideRequestId = backStackEntry.arguments?.getString("rideRequestId") ?: return@composable
            RideRequestDetailScreen(
                requestId = rideRequestId,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onAcceptClick = {
                    navController.navigate(Screen.MySubscribedRides.route) {
                        popUpTo(Screen.MySubscribedRides.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onRiderClick = {riderId ->
                    navController.navigate(Screen.UserDetails.createRoute(riderId))
                }
            )
        }



        composable (
            route = Screen.UserDetails.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ){  backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserDetailsScreen(
                userId = userId,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onMakeReviewClick = { reviewedUserId ->
                    navController.navigate(Screen.MakeReview.createRoute(reviewedUserId))
                },
            )
        }
        composable (
            route = Screen.MakeReview.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ){  backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            MakeReviewScreen(
                userId = userId,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onConfirmClick = {
                    navController.popBackStack()
                },
            )
        }
        composable ( Screen.MyRides.route ){
            MyRidesScreen(
                selectedTab = BottomNavItem.MyRides,
                onTabSelected = {
                        item ->
                    navController.navigate(resolveBottomDestination(item))
                },
                onNewRideClick =  { navController.navigate(Screen.AddNewRide1.route) },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onNewRideRequestClick = {navController.navigate(Screen.AddRequestRide1.route)}
            )
        }
        composable ( Screen.MySubscribedRides.route ){
            MySubscribedRidesScreen(
                selectedTab = BottomNavItem.Schedule,
                onTabSelected = {
                        item ->
                    navController.navigate(resolveBottomDestination(item))
                },
                onUserClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }
    }
}


