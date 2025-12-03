package com.eventify.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventify.app.model.LoginRequest
import com.eventify.app.model.SignupRequest
import com.eventify.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.login(LoginRequest(email, password))
                _token.value = response.token
                _userRole.value = response.role
                _userName.value = response.name
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de connexion")
            }
        }
    }

    fun signup(name: String, email: String, password: String, role: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.signup(SignupRequest(name, email, password, role))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Erreur d'inscription")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur d'inscription")
            }
        }
    }

    fun logout() {
        _token.value = null
        _userRole.value = null
        _userName.value = null
    }
}
