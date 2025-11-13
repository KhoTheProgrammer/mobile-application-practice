package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.*
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.donor.DonationFormScreen
import com.example.myapplication.ui.donor.DonorHomeScreen
import com.example.myapplication.ui.donor.OrphanageDetailScreen
import com.example.myapplication.ui.donor.ThankYouScreen
import com.example.myapplication.ui.donor.ViewMyDonationsScreen
import com.example.myapplication.ui.orphanage.OrphanageHomeScreen
import com.example.myapplication.ui.orphanage.UpdateNeedsScreen
import com.example.myapplication.ui.orphanage.ViewAllDonationsScreen

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
    object SupabaseTest : Screen("supabase_test")
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
            BeautifulLandingPage(
                onGetStartedClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToDonor = {
                    navController.navigate(Screen.DonorHome.route) {
                        popUpTo(Screen.Landing.route) { inclusive = false }
                    }
                },
                onNavigateToOrphanage = {
                    navController.navigate(Screen.OrphanageHome.route) {
                        popUpTo(Screen.Landing.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.DonorHome.route) {
            DonorHomeScreen(
                onOrphanageClick = {
                    navController.navigate(Screen.OrphanageDetail.route)
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
                }
            )
        }

        composable(Screen.OrphanageDetail.route) {
            OrphanageDetailScreen(
                onDonateClick = {
                    navController.navigate(Screen.DonationForm.route)
                }
            )
        }

        composable(Screen.DonationForm.route) {
            DonationFormScreen(
                onSubmitSuccess = {
                    navController.navigate(Screen.ThankYou.route) {
                        popUpTo(Screen.DonorHome.route) { inclusive = false }
                    }
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

        composable(Screen.SupabaseTest.route) {
            com.example.myapplication.ui.test.SupabaseTestScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
