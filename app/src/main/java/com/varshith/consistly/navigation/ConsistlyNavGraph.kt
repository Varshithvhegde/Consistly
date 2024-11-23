package com.varshith.consistly.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.varshith.consistly.ui.screens.AddStreakScreen
import com.varshith.consistly.ui.screens.HomeScreen
import com.varshith.consistly.ui.screens.StreakDetailsScreen
import com.varshith.consistly.viewmodels.StreakViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddStreak : Screen("add_streak")
    object StreakDetails : Screen("streak_details/{streakId}") {
        fun createRoute(streakId: String) = "streak_details/$streakId"
    }
}

@Composable
fun ConsistlyNavGraph(
    navController: NavHostController,
    viewModel: StreakViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            HomeScreen(
                viewModel = viewModel,
                onAddStreak = {
                    navController.navigate(Screen.AddStreak.route)
                },
                onStreakClick = { streakId ->
                    navController.navigate(Screen.StreakDetails.createRoute(streakId))
                }
            )
        }

        composable(
            route = Screen.AddStreak.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            AddStreakScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.StreakDetails.route,
            arguments = listOf(
                navArgument("streakId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val streakId = backStackEntry.arguments?.getString("streakId")
            streakId?.let { id ->
                val streak = viewModel.getStreakById(id) // You'll need to add this method to your ViewModel
                streak?.let {
                    StreakDetailsScreen(
                        streak = it,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}