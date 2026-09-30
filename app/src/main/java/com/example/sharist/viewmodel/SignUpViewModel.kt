package com.example.sharist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.local.util.isValidEmail
import com.example.sharist.data.local.util.normalizeEmail
import com.example.sharist.data.model.UserType
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val passwordConfirmation:String ="",
    val userType: UserType? = null,
    val isSuccess: Boolean = false,
    val passwordError: String? = null,
    val error: String? = null
)


class SignUpViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val db = SharistDatabase.getDatabase(application)
    private val repository = UserRepository(
        dao = db.userDao(),
        remote = StorageRemoteDataSource(),
        local = FileStorageDataSource(),
        authDao = db.authDao()
    )
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private fun validatePasswords(state: SignUpUiState): SignUpUiState {
        val error = if (
            state.password.isNotEmpty() &&
            state.password != state.passwordConfirmation
        ) {
            "Passwords do not match"
        } else {
            null
        }
        return state.copy(passwordError = error)
    }

    fun updateEmail(value: String) {
        _uiState.update {
            it.copy(email = value)
        }
    }

    fun updatePassword(value: String) {
        _uiState.update { current ->
            val newState = current.copy(password = value)
            validatePasswords(newState)
        }
    }
    fun updatePasswordConfirmation(value: String) {
        _uiState.update { current ->
            val newState = current.copy(passwordConfirmation = value)
            validatePasswords(newState)
        }
    }

    fun validateEmail(): Boolean {
        if (!isValidEmail(_uiState.value.email)) {
            return false
        }
        // Persist the normalized form so signup uses the same trimmed/lower-cased value.
        _uiState.update { it.copy(email = normalizeEmail(it.email)) }
        return true
    }

    fun updateUserType(type: UserType) {
        _uiState.update { it.copy(
            userType = type
        ) }
    }

    fun signup() {
        if (!validateEmail()){
            _uiState.update {
                it.copy(
                    error = "Email is not in correct format"
                )
        }
        return
        }
        if (uiState.value.userType==null){
                _uiState.update {
                    it.copy(
                        error = "Must Choose type of user"
                    )
            }
                return
        }
        viewModelScope.launch {
            try {
                repository.signup(
                    email = _uiState.value.email,
                    password = _uiState.value.password,
                    type= _uiState.value.userType!!
                )
                _uiState.update {
                    it.copy(
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message
                    )
                }
            }
        }
    }}

