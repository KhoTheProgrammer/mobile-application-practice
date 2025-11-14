package com.example.myapplication.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.*
import com.example.myapplication.ui.auth.AuthViewModel
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
    val authViewModel: AuthViewModel = viewModel()

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
            com.example.myapplication.ui.auth.AuthScreen(
                viewModel = authViewModel,
                onNavigateToDonor = {
                    android.util.Log.d("NavGraph", "onNavigateToDonor called")
                    navController.navigate(Screen.DonorHome.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                    android.util.Log.d("NavGraph", "Navigation to DonorHome completed")
                },
                onNavigateToOrphanage = {
                    android.util.Log.d("NavGraph", "onNavigateToOrphanage called")
                    navController.navigate(Screen.OrphanageHome.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                    android.util.Log.d("NavGraph", "Navigation to OrphanageHome completed")
                }
            )
        }

        composable(Screen.DonorHome.route) {
            DonorHomeScreen(
                onOrphanageClick = {
                    navController.navigate(Screen.OrphanageDetail.route)
                },
                onLogout = {
                    authViewModel.logout(
                        onSuccess = {
                            // Navigate back to login and clear the back stack
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            // Even if logout fails, navigate to login
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            )
        }

        composable(Screen.OrphanageHome.route) {
            // Observe the auth state to get the current user ID
            val currentUserId = authViewModel.uiState.currentUserId
            android.util.Log.d("NavGraph", "OrphanageHome composable - currentUserId: $currentUserId")
            
            // If no user ID, redirect to login
            if (currentUserId == null || currentUserId.isEmpty()) {
                android.util.Log.d("NavGraph", "No user ID found, redirecting to login")
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                // Show loading while redirecting
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                OrphanageHomeScreen(
                    orphanageId = currentUserId,
                    onViewAllDonations = {
                        navController.navigate(Screen.ViewAllDonations.route)
                    },
                    onUpdateNeeds = {
                        navController.navigate(Screen.UpdateNeeds.route)
                    },
                    onLogout = {
                        authViewModel.logout(
                            onSuccess = {
                                // Navigate back to login and clear the back stack
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onError = { _ ->
                                // Even if logout fails, navigate to login
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                )
            }
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
            val currentUserId = authViewModel.uiState.currentUserId
            if (currentUserId != null) {
                UpdateNeedsScreen(
                    orphanageId = currentUserId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
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
