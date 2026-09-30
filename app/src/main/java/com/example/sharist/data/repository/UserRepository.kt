package com.example.sharist.data.repository


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.sharist.data.local.dao.AuthDao
import com.example.sharist.data.local.dao.UserDao
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.local.util.ImageCompressor
import com.example.sharist.data.model.AuthSessionEntity
import com.example.sharist.data.model.Review
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.model.UserProfile
import com.example.sharist.data.model.UserType
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.remote.SupabaseClient
import com.example.sharist.data.session.CacheManager
import com.example.sharist.data.session.SessionManager
import com.example.sharist.sync.SyncManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class UserRepository(
    private val remote: StorageRemoteDataSource,
    private val local: FileStorageDataSource,
    private val dao: UserDao,
    private val authDao: AuthDao,
) {

    suspend fun signup(
        email: String,
        password: String,
        type: UserType
    ) {
        try{
            val userId = StorageRemoteDataSource().signUp(email, password, type)
            if (userId==null){
                UiEventBus.send(UiEvent.Error("Unable to signup, please confirm you have a connection"))
            }else {
                val userProfile = UserProfile(
                id = userId,
                type = type,
                username = email.substringBefore("@")
                )
                dao.insertUser(userProfile)
        }
        }catch(e:Exception){
            UiEventBus.send(UiEvent.Error("Unable to signup, please confirm you have a connection"))
        }
    }

    private fun processUserImage(context: Context, userProfile: UserProfile, previousLocalPath: String? = null): UserProfile {
        val filename = userProfile.photoUrl?.toUri()?.lastPathSegment
            ?: return userProfile

        val localFileExists = local.fileExists(context, filename)
        val currentLocalFileName = previousLocalPath?.toUri()?.lastPathSegment

        if (localFileExists && currentLocalFileName == filename) {
            Log.d("IMAGE", "local file is valid, no action needed")
            return userProfile.copy(localPhotoPath = previousLocalPath)
        }

        // Delete old cached image using the previous local path
        previousLocalPath?.let {
            Log.d("IMAGE", "Deleting old image")
            local.deleteImage(context, it)
        }

        Log.d("IMAGE", "Downloading image")
        val bytes = local.downloadImageBytes(userProfile.photoUrl)
        Log.d("IMAGE", "Saving new image")
        val localPath = local.saveImage(context, bytes, filename)
        return userProfile.copy(localPhotoPath = localPath)
    }


    suspend fun getProfile(context:Context,userId: String): UserProfile?{
        Log.i("UserRepository", "Checking profile locally")
        CacheManager.get<UserProfile>(userId)?.let { return it } // Cache Hit
        Log.i("UserRepository", "No Cache Hit")
        try{
            // Get supabase profile if cache invalid
            val remoteProfile = remote.getUserProfile(userId)
            Log.i("UserRepository", "Got remoteProfile ${remoteProfile}")
            val local  = dao.getUser(userId)
            val profileChanged =
                local == null ||
                        local.id != remoteProfile.id ||
                        local.type != remoteProfile.type ||
                        local.username != remoteProfile.username ||
                        local.photoUrl != remoteProfile.photoUrl

            val imageChanged = local?.photoUrl != remoteProfile.photoUrl
            Log.i("IMAGE", "Got flags $profileChanged, $imageChanged")
            if (local==null||imageChanged||local.localPhotoPath==null){
                Log.d("IMAGE", "local is null or Image changed")
                // No local User, need to save image and user in database or remote image has changed
                val updatedUserProfile = processUserImage(context,remoteProfile, previousLocalPath = local?.localPhotoPath )
                Log.d("IMAGE", "---- Image Downloaded -> ${updatedUserProfile.toString()}")
                dao.insertUser(updatedUserProfile)
                CacheManager.put<UserProfile>(updatedUserProfile.id, updatedUserProfile)
                return updatedUserProfile
            }
            if (profileChanged){
                Log.d("IMAGE", "Only Profile changed, updating, image is the same")
                //Profile changed but not the image
                dao.insertUser(remoteProfile)
                CacheManager.put<UserProfile>(remoteProfile.id, remoteProfile)
                return remoteProfile
            }
        }catch (e:Exception){
            Log.e("LOADING", "Profile loading error ${e.toString()}")
        }
        Log.d("IMAGE", "Remote not accessible, loading local")
        val local = dao.getUser(userId)
        if (local!=null) CacheManager.put<UserProfile>(local.id, local)
        return local
    }

    suspend fun login(email: String, password: String) {
        Log.d("LOGIN", "Making new Login")
        try{
            val session = remote.login(email, password)
            SessionManager.setSession(session)
            saveSession()
        }catch(e:Exception){
            UiEventBus.send(UiEvent.Error("Unable to Login, please confirm you have a connection"))
        }
    }

    suspend fun loadUserProfileAsync(context:Context, userId: String) {
        withContext(Dispatchers.IO) {
            val profile = getProfile(context,userId) ?: return@withContext
            SessionManager.setUser(profile)
        }
    }

    suspend fun clearSession(){
        authDao.clearSession()
    }
    suspend fun restoreSessionOffline():String{
        Log.d("LOGIN", "Restoring session offline")
        val stored = authDao.getSession() ?: return ""
        val expiresInSeconds =
            ((stored.expiresAt.toEpochMilliseconds() - System.currentTimeMillis())
                .coerceAtLeast(0L)) / 1000L
        SessionManager.setSession(
            UserSession(
                accessToken = stored.accessToken,
                refreshToken = stored.refreshToken,
                expiresIn = expiresInSeconds,
                tokenType = "bearer"
            )
        )
        return stored.userId
    }
    suspend fun saveSession() {
        try{
        val session = remote.getCurrentSession()
        if (session != null) {
            authDao.saveSession(
                AuthSessionEntity(
                    id = 1,
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    expiresAt = session.expiresAt,
                    userId = session.user!!.id
                )
            )
        }}catch(e:Exception){
            UiEventBus.send(UiEvent.Error("Unable to Login, please confirm you have a connection"))
        }
    }
    suspend fun passwordChange(newPassword:String){
        try{
            remote.passwordChange(newPassword)
        }catch(e: Exception){
            UiEventBus.send(UiEvent.Error("Unable to change password, please confirm you have a connection"))
        }
    }

    suspend fun emailChange(newEmail: String) {
        try{
            remote.changeEmail(newEmail)
        }catch(e:Exception){
            UiEventBus.send(UiEvent.Error("Unable to change email, please confirm you have a connection"))
        }
    }

    suspend fun usernameChange(newUsername: String) {
        val user = SessionManager.user.value ?: return
        remote.updateUsername(user.id, newUsername)
        val updated = user.copy(username = newUsername)
        dao.update(updated)
        SessionManager.setUser(updated)
    }

    suspend fun updateRemote(context: Context, user: UserProfile) {
        if (user.photoUrl != null) {
            val fileName = user.photoUrl.substringAfterLast("/")
            remote.deletePhoto("userPhoto", fileName)
        }
        if (user.localPhotoPath != null) {
            // Read directly from the file path, not via content resolver
            val file = File(user.localPhotoPath)
            val bytes = ImageCompressor.compressImageFromFile(file)
            // Or if you need compression, use a File-based overload:
            // val bytes = ImageCompressor.compressImageFromFile(file)

            val remoteUrl = remote.uploadPhoto("userPhoto", bytes)
            dao.update(user.copy(
                photoUrl = remoteUrl,
                syncState = SyncState.SYNCED.toString()
            ))
            remote.updatePhotoUrl(remoteUrl, user.localPhotoPath)
        }
    }

    suspend fun updatePhoto(context: Context, uri: Uri){
        //1 - Delete old picture and save new one locally
        val user = SessionManager.user.value ?: return
        if (user.localPhotoPath!=null){
            local.deleteImage(context, user.localPhotoPath)
        }
        val bytes = ImageCompressor.compressImage(context, uri)
        val localPath = local.saveImage(context, bytes, UUID.randomUUID().toString())


        val updatedUser = user.copy(
            localPhotoPath = localPath,
            syncState = SyncState.PENDING_CREATE.toString()
        )
        dao.update(updatedUser)
        SessionManager.setUser(
            updatedUser
        )
        // 2- Try to delete old and upload new to supabase
        val sync = try{
             updateRemote(context, updatedUser)
        }catch(e: Exception){
            Log.e("IMAGE", "Unable to upload ${e.toString()}")
            null
        }
        if (sync==null){
            UiEventBus.send(UiEvent.Warning("Unable to load user profile, please confirm you have a connection"))
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }
}