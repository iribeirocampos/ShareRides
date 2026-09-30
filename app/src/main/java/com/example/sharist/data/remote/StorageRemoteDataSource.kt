package com.example.sharist.data.remote

import android.util.Log
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.Review
import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.RideRequest
import com.example.sharist.data.model.SimpleLocation
import com.example.sharist.data.model.Subscription
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.model.UserProfile
import com.example.sharist.data.model.UserType
import com.example.sharist.data.model.Vehicle
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Instant

class StorageRemoteDataSource() {
    private val client
        get() = requireNotNull(SupabaseClient.client) {
            "Supabase is not configured"
        }

    suspend fun signOut(){
        client.auth.signOut()
    }
    suspend fun changeEmail(newEmail:String){
        // Supabase sends a confirmation link; the auth email only changes once confirmed.
        client.auth.updateUser {
            email = newEmail
        }
    }

    suspend fun passwordChange(newPassword: String){
        client.auth.updateUser {
            password = newPassword
        }
    }


    suspend fun getCurrentUser(): UserInfo?{
        return client.auth.currentUserOrNull()
    }
     fun getCurrentSession(): UserSession? {
        return client.auth.currentSessionOrNull()
    }
    suspend fun login(email:String, password:String): UserSession?{
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return client.auth.currentSessionOrNull()
    }

    suspend fun signUp(email:String, password:String, type: UserType):String?{
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        // 2. Get user id
        val user = client.auth.currentUserOrNull()
        // 3. Insert profile
        client
            .from("profiles")
            .insert(
                mapOf(
                    "id" to user?.id,
                    "type" to type.toString(),
                    "username" to email.substringBefore("@")
                )
            )
        return user?.id

    }
    suspend fun getUserProfile(userId:String): UserProfile{
        return client
            .from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle()
    }

    suspend fun deletePhoto(
        bucket: String,
        fileName: String
    ) {
        val result = client.storage
            .from(bucket)
            .delete(listOf("private/$fileName"))
        Log.d("Delete", result.toString())
    }

    suspend fun uploadPhoto(
        bucket: String,
        bytes: ByteArray
    ): String = withContext(Dispatchers.IO) {

        val fileName =
            "private/${bucket}_${UUID.randomUUID()}.jpg"

        client.storage
            .from(bucket)
            .upload(
                path = fileName,
                data = bytes
            )
        client.storage
            .from(bucket)
            .publicUrl(fileName)
    }

    suspend fun updatePhotoUrl(
        remoteUrl: String,
        localPath: String
    ) {
        val userId = client.auth.currentUserOrNull()?.id ?: return

        client
            .from("profiles")
            .update({
                set("photoUrl", remoteUrl)
                set("photoLocalPath", localPath)
            }) {
                filter {
                    eq("id", userId)
                }
            }
    }

    suspend fun updateUsername(
        userId: String,
        username: String
    ) {
        client
            .from("profiles")
            .update({
                set("username", username)
            }) {
                filter {
                    eq("id", userId)
                }
            }
    }

    suspend fun insertLocation(location: Location){
        val remoteLocation = location.copy(
            syncState = SyncState.SYNCED.toString()
        )
        client
            .from("location")
            .insert(remoteLocation)
    }

    suspend fun deleteLocation(location:Location){
        client
            .from("location")
            .delete {
                filter {
                    eq("id", location.id)
                }
            }
    }

    suspend fun getUserLocations(userId:String):List<Location>{
        return client
            .from("location")
            .select {
                filter {
                    eq("userId", userId)
                }
            }
            .decodeList()
    }
    suspend fun insertVehicle(vehicle: Vehicle){
        val remoteVehicle = vehicle.copy(
            syncState = SyncState.SYNCED.toString()
        )
        client
            .from("vehicle")
            .insert(remoteVehicle)
    }

    suspend fun removeVehicle(vehicle: Vehicle){
        client
        .from("vehicle")
        .delete {
            filter {
                eq("id", vehicle.id)
        }
    }
}
    suspend fun getUserVehicles(userId:String):List<Vehicle>{
        return client
            .from("vehicle")
            .select {
                filter {
                    eq("userId", userId)
                }
            }
            .decodeList()
    }

    suspend fun getUserRides(userId:String):List<Ride>{
        return client
            .from("ride")
            .select {
                filter {
                    eq("userId", userId)
                }
            }
            .decodeList()
    }

    suspend fun insertRide(ride: Ride){
        val remoteRide = ride.copy(
            syncState = SyncState.SYNCED.toString()
        )
        client
            .from("ride")
            .insert(remoteRide)
    }

    suspend fun deleteRide(ride: Ride){
        client
            .from("ride")
            .delete {
                filter {
                    eq("id", ride.id)
                }
            }
    }

    // Rolls a recurring ride forward to its next occurrence. Updates only the
    // date fields so concurrent changes (e.g. availableSeats from other riders)
    // are preserved. Instants are stored as ISO-8601 strings to match the
    // existing date filters (gt("departureDateTime", now)).
    suspend fun rollRideDates(rideId: String, departure: Instant, arrival: Instant?){
        client
            .from("ride")
            .update({
                set("departureDateTime", departure.toString())
                set("arrivalDateTime", arrival?.toString())
            }) {
                filter {
                    eq("id", rideId)
                }
            }
    }

    // Rolls a recurring ride request forward. Updates only the date so the
    // accepted driverId is preserved.
    suspend fun rollRideRequestDate(requestId: String, date: Instant){
        client
            .from("rideRequest")
            .update({
                set("date", date.toString())
            }) {
                filter {
                    eq("id", requestId)
                }
            }
    }

    suspend fun removeRideRecurrence(rideId: String){
        client
            .from("ride")
            .update({
                set("isWeekly",false)
                set("isDaily",false)
            }) {
                filter {
                    eq("id", rideId)
                }
            }
    }




    // Turns recurrence on/off for a request. When false the request stops being
    // rolled forward and ages out of the upcoming (date > now) queries.
    suspend fun setRideRequestActive(requestId: String, active: Boolean){
        client
            .from("rideRequest")
            .update({
                set("active", active)
            }) {
                filter {
                    eq("id", requestId)
                }
            }
    }

    suspend fun getRide(rideId:String):Ride?{
        return client
            .from("ride")
            .select {
                filter {
                    eq("id", rideId)
                }
            }
            .decodeSingleOrNull<Ride>()
    }

    suspend fun insertRideRequest(rideRequest: RideRequest){
        val remoteRide = rideRequest.copy(
            syncState = SyncState.SYNCED.toString()
        )
        client
            .from("rideRequest")
            .insert(remoteRide)
    }

    suspend fun deleteRideRequest(rideRequest: RideRequest){
        client
            .from("rideRequest")
            .delete {
                filter {
                    eq("id", rideRequest.id)
                }
            }
    }

    suspend fun getUserRideRequests(userId:String):List<RideRequest>{
        return client
            .from("rideRequest")
            .select {
                filter {
                    eq("userId", userId)
                }
            }
            .decodeList()
    }
    suspend fun getRideRequest(rideRequestId:String):RideRequest?{
        return client
            .from("rideRequest")
            .select {
                filter {
                    eq("id", rideRequestId)
                }
            }
            .decodeSingleOrNull<RideRequest>()
    }

    suspend fun cancelRideRequest(rideRequestId: String) {
        client
            .from("rideRequest")
            .update({
                set("driverId", null as String?)
            }) {
                filter {
                    eq("id", rideRequestId)
                }
            }
    }
    suspend fun insertSubscription(subscription: Subscription):String {
        return client.postgrest.rpc(
            function = "subscribe_to_ride",
            parameters = buildJsonObject {
                put("p_subscription_id", subscription.id)
                put("p_user_id", subscription.userRiderId)
                put("p_ride_id", subscription.rideId)
                put("p_only_in_rain", subscription.onlyInRain)
                put("p_reoccurrence", subscription.reoccurrence)
            }
        ).decodeAs<String>()
    }

    suspend fun deleteSubscription(subscription: Subscription):String{
        return client.postgrest.rpc(
            function = "delete_subscription",
            parameters = buildJsonObject {
                put("p_subscription_id", subscription.id)
            }
        ).decodeAs<String>()
    }

    suspend fun acceptRideRequest(requestId: String, driverId: String): String {
        return client.postgrest.rpc(
            function = "accept_ride_request",
            parameters = buildJsonObject {
                put("p_request_id", requestId)
                put("p_driver_id", driverId)
            }
        ).decodeAs()
    }
    suspend fun getUserSubscriptions(userId:String):List<Subscription>{
        return client
            .from("subscription")
            .select {
                filter {
                    eq("userRiderId", userId)
                }
            }
            .decodeList<Subscription>()
    }
    suspend fun getSubscribedRides(rideIds:List<String>):List<Ride>{
        return client
            .from("ride")
            .select {
                filter {
                    isIn("id", rideIds)
                }
            }
            .decodeList<Ride>()
    }

    suspend fun getAvailableRides(location: SimpleLocation, radiusKm: Double = 100.0): List<Ride> {
        return client
            .postgrest
            .rpc(
                function = "get_rides_near",
                parameters = buildJsonObject {
                    put("lat", location.latitude)
                    put("lng", location.longitude)
                    put("radius_km", radiusKm)
                }
            )
            .decodeList<Ride>()
    }
    suspend fun getAvailableRideRequests(location: SimpleLocation, radiusKm: Double = 100.0):List<RideRequest>{
        return client
            .postgrest
            .rpc(
                function = "get_ride_requests_near",
                parameters = buildJsonObject {
                    put("lat", location.latitude)
                    put("lng", location.longitude)
                    put("radius_km", radiusKm)
                }
            )
            .decodeList<RideRequest>()

    }

    suspend fun getUserReviews(userId:String):List<Review>{
        val result = client
            .from("review")
            .select {
                filter {
                   eq("targetUserId", userId)
                }
            }
            .decodeList<Review>()
        return result
    }

    suspend fun insertReview(review:Review){
        val remoteReview = review.copy(
            syncState = SyncState.SYNCED.toString()
        )
        client
            .from("review")
            .insert(remoteReview)
    }
}