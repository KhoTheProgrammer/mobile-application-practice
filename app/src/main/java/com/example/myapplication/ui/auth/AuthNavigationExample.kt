package com.example.myapplication.ui.auth

/**
 * Example of how to integrate the authentication screens with Navigation Compose
 * 
 * Add these routes to your NavHost:
 * 
 * ```kotlin
 * NavHost(
 *     navController = navController,
 *     startDestination = "login"
 * ) {
 *     composable("login") {
 *         LoginScreen(
 *             onNavigateToDonor = { navController.navigate("donor_home") },
 *             onNavigateToOrphanage = { navController.navigate("orphanage_home") },
 *             onNavigateToSignup = { navController.navigate("signup") },
 *             onNavigateToForgotPassword = { navController.navigate("forgot_password") }
 *         )
 *     }
 *     
 *     composable("signup") {
 *         SignupScreen(
 *             onNavigateBack = { navController.popBackStack() },
 *             onNavigateToLogin = { 
 *                 navController.popBackStack("login", inclusive = false)
 *             },
 *             onSignupSuccess = { 
 *                 navController.navigate("donor_home") {
 *                     popUpTo("login") { inclusive = true }
 *                 }
 *             }
 *         )
 *     }
 *     
 *     composable("forgot_password") {
 *         ForgotPasswordScreen(
 *             onNavigateBack = { navController.popBackStack() },
 *             onNavigateToLogin = { 
 *                 navController.popBackStack("login", inclusive = false)
 *             }
 *         )
 *     }
 *     
 *     composable("donor_home") {
 *         // Your donor home screen
 *     }
 *     
 *     composable("orphanage_home") {
 *         // Your orphanage home screen
 *     }
 * }
 * ```
 * 
 * Features implemented:
 * ✅ Email validation with proper error messages
 * ✅ Password validation (minimum 6 characters)
 * ✅ Password visibility toggle
 * ✅ Loading states with progress indicators
 * ✅ Forgot password flow with success state
 * ✅ Signup with user type selection (Donor/Orphanage)
 * ✅ Password confirmation matching
 * ✅ Modern, clean UI with Material 3
 * ✅ Responsive keyboard actions (Next/Done)
 * ✅ Proper focus management
 * ✅ Snackbar notifications
 * ✅ Dark theme support
 * 
 * Demo credentials:
 * - Email: donor@example.com or orphanage@example.com
 * - Password: any password (6+ characters)
 */
