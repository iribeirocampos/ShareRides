package com.example.sharist.data.local.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sharist.data.local.dao.LocationDao
import com.example.sharist.data.local.dao.RideDao
import com.example.sharist.data.local.dao.RideRequestDao
import com.example.sharist.data.local.dao.SubscriptionDao
import com.example.sharist.data.model.AuthSessionEntity
import com.example.sharist.data.local.dao.UserDao
import com.example.sharist.data.local.dao.VehicleDao
import com.example.sharist.data.model.Converters
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.UserProfile
import com.example.sharist.data.model.Vehicle
import com.example.sharist.data.model.Review
import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.RideRequest
import com.example.sharist.data.model.Subscription
import com.example.sharist.data.local.dao.AuthDao
import com.example.sharist.data.local.dao.ReviewDao
@TypeConverters(Converters::class)
@Database(entities = [UserProfile::class,Location::class,Vehicle::class, Ride::class, RideRequest::class, Subscription::class, AuthSessionEntity::class, Review::class], version = 9, exportSchema = false)
abstract class SharistDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun userDao(): UserDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun rideDao(): RideDao
    abstract fun rideRequestDao():RideRequestDao
    abstract  fun subscriptionDao(): SubscriptionDao

    abstract fun reviewDao():ReviewDao

    abstract fun authDao():AuthDao

    companion object {
        @Volatile
        private var INSTANCE: SharistDatabase? = null

        fun getDatabase(context: Context): SharistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SharistDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}