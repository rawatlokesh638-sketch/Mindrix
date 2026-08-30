package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.MainViewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.battle.ModeSelectionScreen
import com.example.ui.screens.gameplay.GameplayScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.result.ResultScreen
import com.example.ui.screens.shop.ShopScreen

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val userStats by viewModel.userStats.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val showBottomNav = currentRoute.contains("Home") ||
            currentRoute.contains("Leaderboard") ||
            currentRoute.contains("Shop") ||
            currentRoute.contains("Profile")

    LaunchedEffect(userStats) {
        if (userStats == null && !currentRoute.contains("Auth")) {
            navController.navigate(Auth) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav && userStats != null) {
                androidx.compose.foundation.layout.Column {
                    com.example.ads.BannerAdView()
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Home) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Auth,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
            exitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(200)) },
            popEnterTransition = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(200)) }
        ) {
            composable<Auth> {
                AuthScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Home) {
                            popUpTo<Auth> { inclusive = true }
                        }
                    }
                )
            }
            composable<Home> {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToBattle = { mode ->
                        if (mode != null) {
                            navController.navigate(Gameplay(mode = mode))
                        } else {
                            navController.navigate(ModeSelection)
                        }
                    }
                )
            }
            composable<ModeSelection> {
                ModeSelectionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onModeSelected = { mode, difficulty ->
                        navController.navigate(Gameplay(mode = mode, difficulty = difficulty))
                    }
                )
            }
            composable<Gameplay> { backStackEntry ->
                val gameplayRoute = backStackEntry.toRoute<Gameplay>()
                GameplayScreen(
                    mode = gameplayRoute.mode,
                    difficulty = gameplayRoute.difficulty,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToResult = {
                        navController.navigate(Result) {
                            popUpTo<ModeSelection> { inclusive = false }
                        }
                    }
                )
            }
            composable<Result> {
                ResultScreen(
                    viewModel = viewModel,
                    onNavigateHome = {
                        navController.navigate(Home) {
                            popUpTo(0)
                        }
                    },
                    onPlayAgain = {
                        val lastMode = viewModel.lastGameMode.value
                        navController.popBackStack()
                        navController.navigate(Gameplay(mode = lastMode))
                    }
                )
            }
            composable<Leaderboard> {
                LeaderboardScreen(viewModel = viewModel)
            }
            composable<Shop> {
                ShopScreen(viewModel = viewModel)
            }
            composable<Profile> {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        navController.navigate(Auth) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
