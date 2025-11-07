package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.donor.DonationFormScreen
import com.example.myapplication.ui.donor.DonorHomeScreen
import com.example.myapplication.ui.LandingPage
import com.example.myapplication.ui.TestingScreen
import com.example.myapplication.ui.auth.ForgotPasswordScreen
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.SignupScreen
import com.example.myapplication.ui.donor.OrphanageDetailScreen
import com.example.myapplication.ui.donor.ThankYouScreen
import com.example.myapplication.ui.orphanage.OrphanageHomeScreen
import com.example.myapplication.ui.orphanage.UpdateNeedsScreen
import com.example.myapplication.ui.orphanage.ViewAllDonationsScreen
import com.example.myapplication.ui.donor.ViewMyDonationsScreen
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.auth.ChangePasswordScreen
import com.example.myapplication.ui.notifications.NotificationsScreen
import com.example.myapplication.ui.payment.PaymentScreen
import com.example.myapplication.ui.donor.DonationReceiptScreen

// Define all routes as a sealed class for type safety
sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object DonorHome : Screen("donor_home")
    object OrphanageHome : Screen("orphanage_home")
    object OrphanageDetail : Screen("orphanage_detail")
    object DonationForm : Screen("donation_form")
    object ThankYou : Screen("thank_you")
    object ViewMyDonations : Screen("view_my_donations")
    object ViewAllDonations : Screen("view_all_donations")
    object UpdateNeeds : Screen("update_needs")
    object Testing : Screen("testing")
    object Profile : Screen("profile")
    object ChangePassword : Screen("change_password")
    object Notifications : Screen("notifications")
    object Payment : Screen("payment")
    object Receipt : Screen("receipt")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Landing.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Landing.route) {
            LandingPage(
                onGetStartedClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable("login") {
                  LoginScreen(
            onNavigateToDonor = { navController.navigate("donor_home") },
                        onNavigateToOrphanage = { navController.navigate("orphanage_home") },
                       onNavigateToSignup = { navController.navigate("signup") },
                       onNavigateToForgotPassword = { navController.navigate("forgot_password") }
                           )
        }

        composable("signup") {
                    SignupScreen(
                         onNavigateBack = { navController.popBackStack() },
                         onNavigateToLogin = {
                                 navController.popBackStack("login", inclusive = false)
                             },
                         onSignupSuccess = {
                                 navController.navigate("donor_home") {
                                    popUpTo("login") { inclusive = true }
                                 }
                             }
                             )
                 }

        composable("forgot_password") {
                     ForgotPasswordScreen(
                         onNavigateBack = { navController.popBackStack() },
                         onNavigateToLogin = {
                                 navController.popBackStack("login", inclusive = false)
                             }
                             )
                 }

        composable(Screen.DonorHome.route) {
            DonorHomeScreen(
                onOrphanageClick = {
                    navController.navigate(Screen.OrphanageDetail.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }

        composable(Screen.OrphanageHome.route) {
            OrphanageHomeScreen(
                onViewAllDonations = {
                    navController.navigate(Screen.ViewAllDonations.route)
                },
                onUpdateNeeds = {
                    navController.navigate(Screen.UpdateNeeds.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }

        composable(Screen.OrphanageDetail.route) {
            OrphanageDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onDonateClick = {
                    navController.navigate(Screen.DonationForm.route)
                }
            )
        }

        composable(Screen.DonationForm.route) {
            DonationFormScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmitSuccess = {
                    // Navigate to payment screen
                    navController.navigate(Screen.ThankYou.route)
                }
            )
        }

        composable(Screen.ThankYou.route) {
            ThankYouScreen(
                onViewMyDonations = {
                    navController.navigate(Screen.ViewMyDonations.route)
                },
                onBackToHome = {
                    navController.navigate(Screen.DonorHome.route) {
                        popUpTo(Screen.DonorHome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ViewMyDonations.route) {
            ViewMyDonationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ViewAllDonations.route) {
            ViewAllDonationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.UpdateNeeds.route) {
            UpdateNeedsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Testing.route){
            TestingScreen(
                onNextClick = {
                    navController.navigate(Screen.Landing.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Change Password Screen
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Notifications Screen
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Payment Screen
        composable(Screen.Payment.route) {
            PaymentScreen(
                orphanageName = "Hope Children's Home",
                category = "Food",
                onNavigateBack = { navController.popBackStack() },
                onPaymentSuccess = { transactionId, amount ->
                    navController.navigate("receipt/$transactionId/$amount") {
                        popUpTo(Screen.DonorHome.route) { inclusive = false }
                    }
                }
            )
        }

        // Receipt Screen with parameters
        composable("receipt/{transactionId}/{amount}") { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
            
            DonationReceiptScreen(
                transactionId = transactionId,
                amount = amount,
                orphanageName = "Hope Children's Home",
                category = "Food",
                paymentMethod = "Visa •••• 4242",
                onNavigateBack = { 
                    navController.navigate(Screen.DonorHome.route) {
                        popUpTo(Screen.DonorHome.route) { inclusive = true }
                    }
                },
                onDownloadReceipt = { 
                    // TODO: Implement download functionality
                },
                onShareReceipt = { 
                    // TODO: Implement share functionality
                }
            )
        }
    }
}
