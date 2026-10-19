package com.cloudreel.mobile.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cloudreel.mobile.data.local.SessionManager
import com.cloudreel.mobile.data.remote.AuthRepository
import com.cloudreel.mobile.ui.auth.AuthViewModel
import com.cloudreel.mobile.ui.auth.LoginScreen
import com.cloudreel.mobile.ui.auth.SignUpScreen
import com.cloudreel.mobile.ui.broadcast.BroadcastScreen
import com.cloudreel.mobile.ui.category.CategorySelectionScreen
import com.cloudreel.mobile.ui.home.HomeViewModel
import com.cloudreel.mobile.ui.livesessions.LiveSessionsScreen
import com.cloudreel.mobile.ui.mesh.CellularBondingMeshScreen
import com.cloudreel.mobile.ui.relay.RelayScreen
import com.cloudreel.mobile.ui.session.HostWorkflowScreen
import com.cloudreel.mobile.ui.session.ParticipantWorkflowScreen
import com.cloudreel.mobile.ui.session.SessionEntryScreen
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
        // Unconditionally navigates to Category Selection Screen after branding animation
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            val splashViewModel = SplashViewModel(sessionManager)
            SplashScreen(
                viewModel = splashViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.CategorySelection.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── 2. CATEGORY SELECTION SCREEN ──────────────────────────────────────
        // Initial post-splash screen with two primary categories:
        // 1. Live Sessions  2. Broadcast
        composable(
            route = Screen.CategorySelection.route,
            enterTransition = { fadeIn(animationSpec = tween(350)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            val homeViewModel = HomeViewModel(sessionManager)
            CategorySelectionScreen(
                viewModel = homeViewModel,
                onNavigateToLiveSessions = {
                    navController.navigate(Screen.LiveSessions.route)
                },
                onNavigateToBroadcast = {
                    navController.navigate(Screen.Broadcast.route)
                },
                onLogout = {
                    sessionManager.clearSession()
                }
            )
        }

        // ── 3. LIVE SESSIONS SCREEN ───────────────────────────────────────────
        // Displays: Start as Host & Join as Participant
        composable(
            route = Screen.LiveSessions.route,
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
            LiveSessionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHost = {
                    navController.navigate(Screen.SessionEntry.createRoute("host"))
                },
                onNavigateToParticipant = {
                    navController.navigate(Screen.SessionEntry.createRoute("participant"))
                }
            )
        }

        // ── 4. BROADCAST SCREEN ───────────────────────────────────────────────
        // Displays: Streamer, Relay, and Cellular Bonding Mesh (Network Contribution)
        composable(
            route = Screen.Broadcast.route,
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
            val homeViewModel = HomeViewModel(sessionManager)
            BroadcastScreen(
                viewModel = homeViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStreamer = {
                    if (sessionManager.isSessionActive()) {
                        navController.navigate(Screen.Streamer.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onNavigateToRelay = {
                    navController.navigate(Screen.Relay.route)
                },
                onNavigateToMesh = {
                    navController.navigate(Screen.CellularBondingMesh.route)
                }
            )
        }

        // ── 5. CELLULAR BONDING MESH (NETWORK CONTRIBUTION) WORKFLOW ──────────
        composable(
            route = Screen.CellularBondingMesh.route,
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
            CellularBondingMeshScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── 6. SHARED SESSION URL / CODE ENTRY SCREEN ─────────────────────────
        composable(
            route = Screen.SessionEntry.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "participant"
                }
            ),
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
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "participant"
            SessionEntryScreen(
                mode = mode,
                onNavigateBack = { navController.popBackStack() },
                onValidationSuccess = { validCode ->
                    if (mode.equals("host", ignoreCase = true)) {
                        navController.navigate(Screen.HostWorkflow.createRoute(validCode)) {
                            popUpTo(Screen.LiveSessions.route) { inclusive = false }
                        }
                    } else {
                        navController.navigate(Screen.ParticipantWorkflow.createRoute(validCode)) {
                            popUpTo(Screen.LiveSessions.route) { inclusive = false }
                        }
                    }
                }
            )
        }

        // ── 7. HOST WORKFLOW DESTINATION ──────────────────────────────────────
        composable(
            route = Screen.HostWorkflow.route,
            arguments = listOf(
                navArgument("code") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
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
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            HostWorkflowScreen(
                sessionCode = code,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── 8. PARTICIPANT WORKFLOW DESTINATION ───────────────────────────────
        composable(
            route = Screen.ParticipantWorkflow.route,
            arguments = listOf(
                navArgument("code") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
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
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            ParticipantWorkflowScreen(
                sessionCode = code,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── 9. LOGIN SCREEN (FOR STREAMER WORKFLOW) ───────────────────────────
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
                // On successful authentication for Streamer, navigate directly to StreamerScreen!
                onNavigateToHome = {
                    navController.navigate(Screen.Streamer.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        // ── 10. SIGN UP SCREEN (FOR STREAMER WORKFLOW) ────────────────────────
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
                // On successful registration for Streamer, navigate directly to StreamerScreen!
                onNavigateToHome = {
                    navController.navigate(Screen.Streamer.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ── 11. STREAMER DESTINATION WORKFLOW ─────────────────────────────────
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

        // ── 12. RELAY DESTINATION WORKFLOW ────────────────────────────────────
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
