package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.model.Role
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    val currentUser: StateFlow<User?> = userRepository.currentUser

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.login(username, password)
            onResult(success)
        }
    }

    fun logout() {
        userRepository.logout()
    }
    
    fun addUser(name: String, username: String, password: String, role: Role) {
        viewModelScope.launch {
            userRepository.addUser(User(name = name, username = username, password = password, role = role))
        }
    }
    
    fun updateUser(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
        }
    }
    
    val allUsers = userRepository.getAllUsers()
}
