package com.example.sharist.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.model.Location
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.sharist.data.local.database.SharistDatabase
import java.util.UUID
import com.example.sharist.data.session.SessionManager

data class MyLocationsUiState(
    val address: String="",
    val tag: String="",
    val photoUri: Uri?=null,
    val latitude: Double =0.0,
    val longitude: Double = 0.0,
    val photoUris: Map<String, Uri?> = emptyMap(),
    val error :String?=null
)

class MyLocationsViewModel( application: Application) : AndroidViewModel(application){
    private val _uiState = MutableStateFlow(MyLocationsUiState())
    val uiState: StateFlow<MyLocationsUiState> = _uiState.asStateFlow()

    private val db = SharistDatabase.getDatabase(application)
    private val repository = LocationRepository(
        dao = db.locationDao(),
        remote = StorageRemoteDataSource(),
        local = FileStorageDataSource()
    )

    fun validateFields():Boolean{
        if (uiState.value.address.isEmpty()){
            _uiState.update {
                it.copy(error = "An address is required")
            }
            return false
        }
        if (uiState.value.tag.isEmpty()){
            _uiState.update {
                it.copy(error = "A tag is required")
            }
            return false
        }
        return true
    }

    fun updateTag(tag:String){
        _uiState.update {
            it.copy(tag=tag)
        }
    }

    fun updatePhotoUri(uri:Uri?){
        _uiState.update{
            it.copy(photoUri=uri)
        }
    }

    fun updateLocation(location: Location){
        _uiState.update {
            it.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                address = location.address
            )
        }
    }

    fun addLocation(context:Context,onSuccess:()->Unit) {
        if(!validateFields()){
            return
        }
        val userId = SessionManager.user.value?.id
        if (userId == null) {
            _uiState.update {
                it.copy(
                    error = "User not authenticated"
                )
            }
            return
        }
        viewModelScope.launch {
        val location = Location(
            id = UUID.randomUUID().toString(),
            name = _uiState.value.tag,
            address = _uiState.value.address,
            userId = userId,
            latitude = _uiState.value.latitude,
            longitude = _uiState.value.longitude,
            remotePhotoUrl= _uiState.value.photoUri?.toString()
        )
        repository.add(context, location)
        onSuccess()
        }

    }

    fun removeLocation(context:Context,location: Location) {
        viewModelScope.launch{
            repository.remove(context ,location)
        }
    }


    fun get(locationId:String):Location?{
        return repository.get(locationId)
    }

    fun getLocations(): StateFlow<List<Location>>{
        return repository.getLocations()
    }

}
