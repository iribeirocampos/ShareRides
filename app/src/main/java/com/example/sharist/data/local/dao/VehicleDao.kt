package com.example.sharist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.sharist.data.model.Vehicle

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicle")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicle WHERE id = :id")
    fun getVehicle(id:String): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    @Query("SELECT * FROM vehicle WHERE userId=:userId AND syncState!='PENDING_DELETE'")
    suspend fun getUserVehicles(userId:String):List<Vehicle>

    @Update
    suspend fun updateVehicle(vehicle:Vehicle)

    @Query("SELECT * FROM vehicle WHERE syncState == 'PENDING_CREATE'")
    suspend fun getPendingVehicles(): List<Vehicle>

    @Query("SELECT * FROM vehicle WHERE syncState == 'PENDING_DELETE'")
    suspend fun getPendingVehicleDelete(): List<Vehicle>
}
