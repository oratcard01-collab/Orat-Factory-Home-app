package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.StockViewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.StockScreen
import com.example.ui.screens.CategoryStockScreen
import com.example.ui.screens.UsersScreen
import com.example.ui.screens.SearchScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.net.URLDecoder

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    stockViewModel: StockViewModel
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) "login" else "dashboard"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                authViewModel = authViewModel,
                stockViewModel = stockViewModel,
                onNavigateToAdmin = { navController.navigate("admin") },
                onNavigateToStock = { navController.navigate("stock") },
                onNavigateToScan = { mode -> navController.navigate("scan/$mode") },
                onNavigateToUsers = { navController.navigate("users") },
                onNavigateToSearch = { navController.navigate("search") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        composable("scan/{mode}") { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "lookup"
            ScanScreen(
                authViewModel = authViewModel,
                stockViewModel = stockViewModel,
                mode = mode,
                onBack = { navController.popBackStack() }
            )
        }
        composable("stock") {
            StockScreen(
                onNavigateToCategory = { category ->
                    val encoded = URLEncoder.encode(category, StandardCharsets.UTF_8.toString())
                    navController.navigate("category_stock/$encoded")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("category_stock/{category}") { backStackEntry ->
            val encodedCategory = backStackEntry.arguments?.getString("category") ?: ""
            val category = URLDecoder.decode(encodedCategory, StandardCharsets.UTF_8.toString())
            CategoryStockScreen(
                category = category,
                stockViewModel = stockViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("admin") {
            AdminScreen(
                authViewModel = authViewModel,
                stockViewModel = stockViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("users") {
            UsersScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("search") {
            SearchScreen(
                stockViewModel = stockViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
