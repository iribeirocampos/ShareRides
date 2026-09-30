package com.example.sharist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PasswordUiState(
    val currentPassword:String="",
    val password: String = "",
    val confirmedPassword: String = "",
    val error: String?=null,
    val successMessage:String?=null
)


class ChangePasswordViewModel(
    application: Application
): AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PasswordUiState())
    val uiState = _uiState.asStateFlow()
    private val db = SharistDatabase.getDatabase(application)
    private val repository = UserRepository(
        dao = db.userDao(),
        remote = StorageRemoteDataSource(),
        local = FileStorageDataSource(),
        authDao = db.authDao()
    )
    fun updateError(error:String){
        _uiState.update{
            it.copy(
                error=error
            )
        }
    }
    suspend fun validateFields(): Boolean {
        val user = StorageRemoteDataSource().getCurrentUser()
        if (user?.email == null) {
            updateError("User must be authenticated, please login")
            return false
        }

        // 1. async check FIRST (must be awaited)
        try {
            repository.login(
                email = user.email!!,
                password = _uiState.value.currentPassword
            )
        } catch (e: Exception) {
            updateError("Current Password is incorrect")
            return false
        }

        // 2. sync checks AFTER async succeeds
        if (_uiState.value.password.isEmpty() ||
            _uiState.value.confirmedPassword.isEmpty()
        ) {
            updateError("Password and Password Confirmation cannot be empty")
            return false
        }

        if (_uiState.value.password != _uiState.value.confirmedPassword) {
            updateError("New password and Password confirmation must be the same")
            return false
        }

        return true
    }
    fun clearSuccessMessage() {
        _uiState.update {
            it.copy(successMessage = null)
        }
    }

    fun  changePassword(onSuccess:()->Unit){
        viewModelScope.launch {
            if (!validateFields()){
                return@launch
            }
            try {
                repository.passwordChange(
                    newPassword = _uiState.value.password
                )
                _uiState.update {
                    it.copy(
                        currentPassword = "",
                        password = "",
                        confirmedPassword = "",
                        error = null,
                        successMessage = "Password changed successfully"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        currentPassword = "",
                        password = "",
                        confirmedPassword = "",
                        error = e.message)
                }
            }
        }
    }

    fun updateCurrentPassword(value:String){
        _uiState.update{
            it.copy(currentPassword=value)
        }
    }
    fun updatePassword(value: String) {
        _uiState.update {
            it.copy(password = value)
        }
    }


    fun updateConfirmedPassword(value: String) {
        _uiState.update {
            it.copy(confirmedPassword = value)
        }
    }
}