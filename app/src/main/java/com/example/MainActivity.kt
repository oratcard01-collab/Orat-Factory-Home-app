package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.repository.StockRepository
import com.example.data.repository.UserRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModelFactory
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.StockViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val db = AppDatabase.getDatabase(this)
    val userRepository = UserRepository(db.userDao())
    val stockRepository = StockRepository(db.stockItemDao(), db.deductionLogDao())
    
    val factory = AppViewModelFactory(userRepository, stockRepository)
    
    val authViewModel: AuthViewModel by viewModels { factory }
    val stockViewModel: StockViewModel by viewModels { factory }
    
    lifecycleScope.launch {
        userRepository.initializeSuperAdmin()
    }

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            AppNavigation(
                authViewModel = authViewModel,
                stockViewModel = stockViewModel
            )
        }
      }
    }
  }
}

