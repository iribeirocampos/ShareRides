package com.example.sharist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sharist.data.model.AuthSessionEntity


@Dao
interface AuthDao {

    @Query("SELECT * FROM auth_session WHERE id = 1 LIMIT 1")
    suspend fun getSession(): AuthSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: AuthSessionEntity)

    @Query("DELETE FROM auth_session")
    suspend fun clearSession()
}