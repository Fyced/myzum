package com.example.myzum.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myzum.model.User
import com.example.myzum.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UserState {
    object Loading : UserState()
    data class Registered(val user: User) : UserState()
    object Unregistered : UserState()
}

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _activeUserId = MutableStateFlow<String?>(null)
    val activeUserId: StateFlow<String?> = _activeUserId.asStateFlow()

    init {
        // Observe if there is a logged in user
        viewModelScope.launch {
            userRepository.getLoggedInUser().collect { user ->
                if (user != null) {
                    _activeUserId.value = user.id
                    _userState.value = UserState.Registered(user)
                } else {
                    _activeUserId.value = null
                    _userState.value = UserState.Unregistered
                }
            }
        }
    }

    fun login(id: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = userRepository.findUserById(id)
            if (user != null && user.password == password) {
                val loggedUser = user.copy(isLoggedIn = true)
                userRepository.saveUser(loggedUser)
                // The init observer will handle the state update
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            val registeredUser = user.copy(isLoggedIn = true)
            userRepository.saveUser(registeredUser)
            // The init observer will handle the state update
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentState = _userState.value
            if (currentState is UserState.Registered) {
                val user = currentState.user
                userRepository.saveUser(user.copy(isLoggedIn = false))
            }
            _activeUserId.value = null
            _userState.value = UserState.Unregistered
        }
    }

    fun updateNotifications(enabled: Boolean) {
        updateSettings { it.copy(notificationsEnabled = enabled) }
    }

    fun updateBiometric(enabled: Boolean) {
        updateSettings { it.copy(biometricEnabled = enabled) }
    }

    private fun updateSettings(transform: (com.example.myzum.model.UserSettings) -> com.example.myzum.model.UserSettings) {
        val currentState = _userState.value
        if (currentState is UserState.Registered) {
            val user = currentState.user
            val updatedUser = user.copy(settings = transform(user.settings))
            register(updatedUser)
        }
    }
}
