package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Role
import com.example.data.model.User
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val users by authViewModel.allUsers.collectAsState(initial = emptyList())
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSuperAdmin = currentUser?.role == Role.SUPER_ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!isSuperAdmin) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Access Denied: Super Admin only.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(users) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(user.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Username: ${user.username}")
                                    Text("Role: ${user.role.name}", color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { userToEdit = user }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit User")
                                }
                            }
                        }
                    }
                }
                
                FloatingActionButton(
                    onClick = { showAddUserDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add User")
                }
            }
        }

        if (userToEdit != null) {
            val user = userToEdit!!
            var editName by remember { mutableStateOf(user.name) }
            var editUsername by remember { mutableStateOf(user.username) }
            var editPassword by remember { mutableStateOf(user.password) }
            var editRole by remember { mutableStateOf(user.role) }

            AlertDialog(
                onDismissRequest = { userToEdit = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (editName.isNotBlank() && editUsername.isNotBlank() && editPassword.isNotBlank()) {
                                authViewModel.updateUser(user.copy(
                                    name = editName,
                                    username = editUsername,
                                    password = editPassword,
                                    role = editRole
                                ))
                                userToEdit = null
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToEdit = null }) { Text("Cancel") }
                },
                title = { Text("Edit User") },
                text = {
                    Column {
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") })
                        OutlinedTextField(value = editUsername, onValueChange = { editUsername = it }, label = { Text("Username") })
                        OutlinedTextField(value = editPassword, onValueChange = { editPassword = it }, label = { Text("Password") })
                        Text("Role:", modifier = Modifier.padding(top = 8.dp))
                        Role.values().forEach { role ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = editRole == role,
                                    onClick = { editRole = role }
                                )
                                Text(role.name)
                            }
                        }
                    }
                }
            )
        }

        if (showAddUserDialog) {
            var newName by remember { mutableStateOf("") }
            var newUsername by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var newRole by remember { mutableStateOf(Role.GENERAL_STAFF) }

            AlertDialog(
                onDismissRequest = { showAddUserDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank() && newUsername.isNotBlank() && newPassword.isNotBlank()) {
                                authViewModel.addUser(newName, newUsername, newPassword, newRole)
                                showAddUserDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddUserDialog = false }) { Text("Cancel") }
                },
                title = { Text("Add User") },
                text = {
                    Column {
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                        OutlinedTextField(value = newUsername, onValueChange = { newUsername = it }, label = { Text("Username") })
                        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Password") })
                        Text("Role:", modifier = Modifier.padding(top = 8.dp))
                        Role.values().forEach { role ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = newRole == role,
                                    onClick = { newRole = role }
                                )
                                Text(role.name)
                            }
                        }
                    }
                }
            )
        }
    }
}
