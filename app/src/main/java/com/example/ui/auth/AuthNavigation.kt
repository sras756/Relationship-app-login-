package com.example.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.auth.viewmodel.AuthViewModel

@Composable
fun AuthNavigation(onAuthComplete: () -> Unit) {
    val navController = rememberNavController()
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("login") {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = { 
                    navController.navigate("register") {
                        popUpTo("welcome")
                    }
                },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                onLoginSuccess = onAuthComplete,
                viewModel = authViewModel
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { 
                    navController.navigate("login") {
                        popUpTo("welcome")
                    }
                },
                onRegisterSuccess = onAuthComplete,
                viewModel = authViewModel
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }
    }
}

