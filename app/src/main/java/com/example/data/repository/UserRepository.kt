package com.example.data.repository

import com.example.data.dao.UserDao
import com.example.data.model.Role
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepository(private val userDao: UserDao) {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun initializeSuperAdmin() {
        if (userDao.getUserCount() == 0) {
            val superAdmin = User(
                name = "Super Admin",
                username = "admin",
                password = "password", // default password
                role = Role.SUPER_ADMIN
            )
            userDao.insertUser(superAdmin)
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        val user = userDao.getUserByUsername(username)
        if (user != null && user.password == password) {
            _currentUser.value = user
            return true
        }
        return false
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }
    
    fun getAllUsers() = userDao.getAllUsers()
    
    suspend fun addUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}
