package com.cloudreel.mobile.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cloudreel.mobile.data.local.SessionManager
import com.cloudreel.mobile.data.remote.AuthRepository
import com.cloudreel.mobile.ui.auth.AuthViewModel
import com.cloudreel.mobile.ui.auth.LoginScreen
import com.cloudreel.mobile.ui.auth.SignUpScreen
import com.cloudreel.mobile.ui.home.GoLiveHomeScreen
import com.cloudreel.mobile.ui.home.HomeViewModel
import com.cloudreel.mobile.ui.relay.RelayScreen
import com.cloudreel.mobile.ui.splash.SplashScreen
import com.cloudreel.mobile.ui.splash.SplashViewModel
import com.cloudreel.mobile.ui.streamer.StreamerScreen

@Composable
fun CloudReelNavGraph(
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // ── 1. SPLASH SCREEN ──────────────────────────────────────────────────
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            val splashViewModel = SplashViewModel(sessionManager)
            SplashScreen(
                viewModel = splashViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── 2. LOGIN SCREEN ───────────────────────────────────────────────────
        composable(
            route = Screen.Login.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            val authViewModel = AuthViewModel(authRepository)
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        // ── 3. SIGN UP SCREEN ─────────────────────────────────────────────────
        composable(
            route = Screen.SignUp.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            val authViewModel = AuthViewModel(authRepository)
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ── 4. GO LIVE HOME SCREEN ────────────────────────────────────────────
        composable(
            route = Screen.Home.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250))
            }
        ) {
            val homeViewModel = HomeViewModel(sessionManager)
            GoLiveHomeScreen(
                viewModel = homeViewModel,
                onNavigateToStreamer = {
                    navController.navigate(Screen.Streamer.route)
                },
                onNavigateToRelay = {
                    navController.navigate(Screen.Relay.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── 5. STREAMER DESTINATION WORKFLOW ──────────────────────────────────
        composable(
            route = Screen.Streamer.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            StreamerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── 6. RELAY DESTINATION WORKFLOW ─────────────────────────────────────
        composable(
            route = Screen.Relay.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            RelayScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
