package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.StockViewModel
import com.example.data.model.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    stockViewModel: StockViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val stockItems by stockViewModel.allStockItems.collectAsState()
    val users by authViewModel.allUsers.collectAsState(initial = emptyList())
    val currentUser by authViewModel.currentUser.collectAsState()
    
    val isSuperAdmin = currentUser?.role == Role.SUPER_ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search items, categories...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            if (searchQuery.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Type to start searching", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val lowercaseQuery = searchQuery.lowercase()
                
                val matchedStock = stockItems.filter {
                    it.name.lowercase().contains(lowercaseQuery) || 
                    it.category.lowercase().contains(lowercaseQuery) || 
                    (it.uid?.lowercase()?.contains(lowercaseQuery) ?: false)
                }
                
                val matchedUsers = if (isSuperAdmin) {
                    users.filter {
                        it.name.lowercase().contains(lowercaseQuery) ||
                        it.username.lowercase().contains(lowercaseQuery)
                    }
                } else emptyList()
                
                if (matchedStock.isEmpty() && matchedUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found for \"$searchQuery\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        if (matchedUsers.isNotEmpty()) {
                            item {
                                Text("Users", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(matchedUsers) { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(user.name, style = MaterialTheme.typography.bodyLarge)
                                        Text("@${user.username} - ${user.role.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        
                        if (matchedStock.isNotEmpty()) {
                            item {
                                Text("Inventory", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(matchedStock) { item ->
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
                                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
                                            Text(item.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (item.uid != null) {
                                                Text("UID: ${item.uid}", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        Text("Qty: ${item.quantity}", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
