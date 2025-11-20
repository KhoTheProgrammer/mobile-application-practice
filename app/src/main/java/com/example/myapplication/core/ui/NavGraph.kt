package com.example.myapplication.core.ui

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
import com.example.myapplication.auth.domain.AuthViewModel
import com.example.myapplication.donor.ui.DonationFormScreen
import com.example.myapplication.donor.ui.DonorHomeScreen
import com.example.myapplication.donor.ui.OrphanageDetailScreen
import com.example.myapplication.donor.domain.OrphanageDetailViewModel
import com.example.myapplication.donor.domain.OrphanageDetailViewModelFactory
import com.example.myapplication.donor.ui.ThankYouScreen
import com.example.myapplication.donor.ui.ViewMyDonationsScreen
import com.example.myapplication.donor.domain.ViewMyDonationsViewModel
import com.example.myapplication.donor.domain.ViewMyDonationsViewModelFactory
import com.example.myapplication.orphanage.ui.OrphanageHomeScreen
import com.example.myapplication.orphanage.ui.UpdateNeedsScreen
import com.example.myapplication.orphanage.ui.ViewAllDonationsScreen

// Define all routes as a sealed class for type safety
sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object DonorHome : Screen("donor_home")
    object OrphanageHome : Screen("orphanage_home")
    object OrphanageDetail : Screen("orphanage_detail/{orphanageId}") {
        fun createRoute(orphanageId: String) = "orphanage_detail/$orphanageId"
    }
    object DonationForm : Screen("donation_form/{orphanageId}/{orphanageName}") {
        fun createRoute(orphanageId: String, orphanageName: String) = "donation_form/$orphanageId/$orphanageName"
    }
    object ThankYou : Screen("thank_you")
    object ViewMyDonations : Screen("view_my_donations")
    object ViewAllDonations : Screen("view_all_donations")
    object UpdateNeeds : Screen("update_needs")
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
            com.example.myapplication.auth.ui.AuthScreen(
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
                onOrphanageClick = { orphanageId ->
                    navController.navigate(Screen.OrphanageDetail.createRoute(orphanageId))
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

        composable(Screen.OrphanageHome.route) {
            val currentUserId = authViewModel.uiState.currentUserId
            android.util.Log.d("NavGraph", "OrphanageHome composable - currentUserId: $currentUserId")
            
            if (currentUserId.isNullOrEmpty()) {
                android.util.Log.d("NavGraph", "No user ID found, redirecting to login")
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onError = { _ ->
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                )
            }
        }

        composable(Screen.OrphanageDetail.route) { backStackEntry ->
            val orphanageId = backStackEntry.arguments?.getString("orphanageId") ?: ""
            val viewModel: OrphanageDetailViewModel = viewModel(
                factory = OrphanageDetailViewModelFactory(orphanageId)
            )
            OrphanageDetailScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onDonateClick = {
                    val orphanageName = viewModel.uiState.orphanage?.name ?: "Unknown"
                    navController.navigate(Screen.DonationForm.createRoute(orphanageId, orphanageName))
                }
            )
        }

        composable(Screen.DonationForm.route) { backStackEntry ->
            val orphanageId = backStackEntry.arguments?.getString("orphanageId") ?: ""
            val orphanageName = backStackEntry.arguments?.getString("orphanageName") ?: "Unknown"
            val currentUserId = authViewModel.uiState.currentUserId ?: ""
            
            // Use a placeholder category ID - the actual category will be determined from the form
            // We'll fetch the first available category or create a general one
            val generalCategoryId = "00000000-0000-0000-0000-000000000001"
            
            val donationViewModel: com.example.myapplication.donor.domain.DonationFormViewModel = viewModel(
                factory = com.example.myapplication.donor.domain.DonationFormViewModelFactory(
                    orphanageId = orphanageId,
                    orphanageName = orphanageName,
                    categoryId = generalCategoryId
                )
            )
            
            // Set the donor ID
            LaunchedEffect(currentUserId) {
                if (currentUserId.isNotEmpty()) {
                    donationViewModel.setDonorId(currentUserId)
                }
            }
            
            DonationFormScreen(
                viewModel = donationViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
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
            val currentUserId = authViewModel.uiState.currentUserId ?: ""
            val viewModel: ViewMyDonationsViewModel = viewModel(
                factory = ViewMyDonationsViewModelFactory(currentUserId)
            )
            ViewMyDonationsScreen(
                viewModel = viewModel,
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
    }
}
