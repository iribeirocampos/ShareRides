package com.example.sharist.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.local.util.isValidPortugueseLicencePlate
import com.example.sharist.data.local.util.normalizeLicencePlate
import com.example.sharist.data.model.Vehicle
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.remote.SupabaseClient
import com.example.sharist.data.repository.UserRepository
import com.example.sharist.data.repository.VehicleRepository
import com.example.sharist.data.session.SessionManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.plus


data class MyVehiclesUiState(
    val brand : String  ="",
    val model: String="",
    val capacity: Int=0,
    val color: String = "",
    val licencePlate: String="",
    val photoUri: Uri? = null,
    val userID: String = "",
    val error: String? =null
)

class MyVehiclesViewModel(
    application: Application
): AndroidViewModel(application){
    private val _uiState = MutableStateFlow(MyVehiclesUiState())
    val uiState: StateFlow<MyVehiclesUiState> = _uiState.asStateFlow()
    private val db = SharistDatabase.getDatabase(application)
    private val repository = VehicleRepository(
        dao = db.vehicleDao(),
        remote = StorageRemoteDataSource(),
        local = FileStorageDataSource()
    )
    val vehicles = SessionManager.myVehicles

    fun addVehicle(context: Context, onSuccess: () -> Unit) {
        if (!validateAddNewVehicle()){
            return
        }
        val id = UUID.randomUUID().toString()
        val userId = StorageRemoteDataSource().getCurrentSession()?.user?.id
        if (userId == null) {
            _uiState.update {
                it.copy(
                    error = "User not authenticated"
                )
            }
            return
        }
        viewModelScope.launch {
        val vehicle = Vehicle(
            id = id,
            color = _uiState.value.color,
            brand = _uiState.value.brand,
            model = _uiState.value.model,
            licencePlate = normalizeLicencePlate(_uiState.value.licencePlate),
            capacity = _uiState.value.capacity,
            userId = userId,
            remotePhotoUrl = _uiState.value.photoUri?.toString()
        )
        repository.add(context,vehicle)
        onSuccess()
        }
    }

    fun updateBrand(brand:String){
        _uiState.update {
            it.copy(brand=brand)
        }
    }
    fun updateUri(uri: Uri){
        _uiState.update {
            it.copy(photoUri = uri)
        }
    }
    fun updateModel(model:String){
        _uiState.update {
            it.copy(model=model)
        }
    }

    fun updateCapacity(value: String) {
        _uiState.update {
            it.copy(
                capacity = value.toIntOrNull() ?: 0
            )
        }
    }
    fun updateColor(color:String){
        _uiState.update {
            it.copy(color=color)
        }
    }

    fun updateLicencePlate(licence:String){
        _uiState.update {
            it.copy(licencePlate  =licence)
        }
    }

    fun removeVehicle(context:Context,vehicle: Vehicle) {
        viewModelScope.launch{
            repository.remove(context,vehicle)
        }
    }

    fun validateAddNewVehicle():Boolean{
        if (_uiState.value.brand.isEmpty()){
            _uiState.update {
                it.copy(error = "A brand is required")
            }
            return false
        }
        if (_uiState.value.model.isEmpty()){
            _uiState.update {
                it.copy(error = "A Model is required")
            }
            return false
        }
        if (_uiState.value.capacity == 0){
            _uiState.update {
                it.copy(error = "A capacity must be higher then 0")
            }
            return false
        }
        if (_uiState.value.licencePlate.isBlank()){
            _uiState.update {
                it.copy(error = "A Licence Plate is required")
            }
            return false
        }
        if (!isValidPortugueseLicencePlate(_uiState.value.licencePlate)){
            _uiState.update {
                it.copy(error = "Invalid licence plate. Use the Portuguese format (e.g. AA-00-AA)")
            }
            return false
        }
        return true
    }

    fun getVehicle(vehicleId:String):Vehicle?{
        return SessionManager.getVehicle(vehicleId)
    }

    fun updateError(error:String){
        _uiState.update {
            it.copy(error=error)
        }
    }


}