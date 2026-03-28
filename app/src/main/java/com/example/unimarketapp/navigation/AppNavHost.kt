package com.example.unimarketapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.unimarketapp.ui.screens.*
import androidx.compose.material3.Text

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onSignupClick = { navController.navigate(AppRoutes.SIGNUP) }
            )
        }
        composable(AppRoutes.SIGNUP) {
            SignupScreen(
                onSignupSuccess = { 
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.SIGNUP) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }
        composable(AppRoutes.HOME) {
            HomeScreen(navController)
        }
        composable(AppRoutes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.PRODUCT_DETAIL) {
            Box(contentAlignment = Alignment.Center) { Text("Use product_details/{id} route instead") }
        }
        composable(AppRoutes.ADD_PRODUCT) {
            AddProductScreen(navController)
        }
        composable(AppRoutes.CHAT) {
            ChatListScreen(navController)
        }
        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
