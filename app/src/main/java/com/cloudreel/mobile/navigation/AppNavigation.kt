package com.cloudreel.mobile.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.cloudreel.mobile.ui.category.CategorySelectionScreen
import com.cloudreel.mobile.ui.home.HomeViewModel
import com.cloudreel.mobile.ui.mesh.CellularBondingMeshScreen
import com.cloudreel.mobile.ui.relay.RelayScreen
import com.cloudreel.mobile.ui.session.ActiveSessionScreen
import com.cloudreel.mobile.ui.session.DeviceSetupScreen
import com.cloudreel.mobile.ui.session.HostWorkflowScreen
import com.cloudreel.mobile.ui.session.LeftSessionScreen
import com.cloudreel.mobile.ui.session.ParticipantWorkflowScreen
import com.cloudreel.mobile.ui.session.SessionEntryScreen
import com.cloudreel.mobile.ui.session.WaitingLobbyScreen
import com.cloudreel.mobile.ui.splash.SplashScreen
import com.cloudreel.mobile.ui.splash.SplashViewModel
import com.cloudreel.mobile.ui.streamer.RecordingsScreen
import com.cloudreel.mobile.ui.streamer.StreamerRole
import com.cloudreel.mobile.ui.streamer.StreamerScreen
import com.cloudreel.mobile.ui.streamer.StreamerSettingsScreen
import com.cloudreel.mobile.ui.streamer.StreamerViewModel

@Composable
fun CloudReelNavGraph(
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // The camera needs a signed-in account; Home and Broadcast both open it this way.
    fun openStreamer() {
        navController.navigate(if (sessionManager.isSessionActive()) Screen.Streamer.createRoute() else Screen.Login.route)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        // Native push/pop: forward slides the new screen in from the end, Back reverses it.
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
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

        // ── 2. HOME (CATEGORY SELECTION) SCREEN ───────────────────────────────
        // Join a debate, Report live, then direct rows for host, relay and network contribution.
        composable(
            route = Screen.CategorySelection.route,
            // Fade in from the splash; Back into home still uses the NavHost slide.
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            popEnterTransition = null
        ) {
            val homeViewModel = HomeViewModel(sessionManager)
            CategorySelectionScreen(
                viewModel = homeViewModel,
                onJoinWithCode = {
                    navController.navigate(Screen.SessionEntry.createRoute("participant"))
                },
                onGoLive = ::openStreamer,
                onHostDebate = {
                    navController.navigate(Screen.SessionEntry.createRoute("host"))
                },
                onRelay = {
                    navController.navigate(Screen.Relay.route)
                },
                onNetworkContribution = {
                    navController.navigate(Screen.CellularBondingMesh.route)
                },
                onLogout = {
                    sessionManager.clearSession()
                }
            )
        }

        // ── 5. CELLULAR BONDING MESH (NETWORK CONTRIBUTION) WORKFLOW ──────────
        composable(
            route = Screen.CellularBondingMesh.route
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
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "participant"
            SessionEntryScreen(
                mode = mode,
                onNavigateBack = { navController.popBackStack() },
                onValidationSuccess = { validCode ->
                    navController.navigate(Screen.DeviceSetup.createRoute(mode, validCode))
                }
            )
        }

        // ── 7. DEVICE SETUP SCREEN (PRE-JOIN SCREENSHOT 1) ────────────────────
        composable(
            route = Screen.DeviceSetup.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "host"
                },
                navArgument("code") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "host"
            val code = backStackEntry.arguments?.getString("code") ?: ""
            DeviceSetupScreen(
                mode = mode,
                sessionCode = code,
                onNavigateBack = { navController.popBackStack() },
                // The host opens the meeting; a participant waits in the lobby until the host lets them in.
                onStartSession = {
                    navController.navigate(
                        if (mode == "host") Screen.Streamer.createRoute(StreamerRole.HOST, code) else Screen.WaitingLobby.createRoute(code)
                    )
                }
            )
        }

        composable(
            route = Screen.WaitingLobby.route,
            arguments = listOf(navArgument("code") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code").orEmpty()
            WaitingLobbyScreen(
                sessionCode = code,
                onLeave = { navController.popBackStack() },
                onAdmitted = {
                    navController.navigate(Screen.Streamer.createRoute(StreamerRole.GUEST, code)) {
                        popUpTo(Screen.WaitingLobby.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.LeftSession.route,
            arguments = listOf(
                navArgument("role") { type = NavType.StringType; defaultValue = StreamerRole.HOST.name },
                navArgument("code") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val role = StreamerRole.valueOf(backStackEntry.arguments?.getString("role") ?: StreamerRole.HOST.name)
            val code = backStackEntry.arguments?.getString("code").orEmpty()
            LeftSessionScreen(
                onRejoin = {
                    navController.navigate(Screen.Streamer.createRoute(role, code)) {
                        popUpTo(Screen.CategorySelection.route)
                    }
                },
                onGoHome = { navController.popBackStack(Screen.CategorySelection.route, inclusive = false) }
            )
        }

        // ── 8. ACTIVE LIVE SESSION SCREEN (MEETING SCREENSHOT 2) ──────────────
        composable(
            route = Screen.ActiveSession.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "host"
                },
                navArgument("code") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "host"
            val code = backStackEntry.arguments?.getString("code") ?: ""
            ActiveSessionScreen(
                mode = mode,
                sessionCode = code,
                onNavigateBack = {
                    navController.popBackStack(Screen.CategorySelection.route, inclusive = false)
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
            )
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
            )
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            ParticipantWorkflowScreen(
                sessionCode = code,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── 9. LOGIN SCREEN (FOR STREAMER WORKFLOW) ───────────────────────────
        composable(
            route = Screen.Login.route
        ) {
            val authViewModel = remember { AuthViewModel(authRepository) }
            LoginScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                // On successful authentication for Streamer, navigate directly to StreamerScreen!
                onNavigateToHome = {
                    navController.navigate(Screen.Streamer.createRoute()) {
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
            route = Screen.SignUp.route
        ) {
            val authViewModel = AuthViewModel(authRepository)
            SignUpScreen(
                viewModel = authViewModel,
                // On successful registration for Streamer, navigate directly to StreamerScreen!
                onNavigateToHome = {
                    navController.navigate(Screen.Streamer.createRoute()) {
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
            arguments = listOf(
                navArgument("role") { type = NavType.StringType; defaultValue = "reporter" },
                navArgument("code") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val streamerViewModel: StreamerViewModel = viewModel()
            val role = StreamerRole.valueOf(backStackEntry.arguments?.getString("role") ?: StreamerRole.REPORTER.name)
            val code = backStackEntry.arguments?.getString("code").orEmpty()
            StreamerScreen(
                viewModel = streamerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenRecordings = { navController.navigate(Screen.Recordings.route) },
                onOpenSettings = { navController.navigate(Screen.StreamerSettings.route) },
                role = role,
                sessionCode = code,
                // Ending closes the session, so home; leaving keeps it running, so offer to rejoin.
                onLeaveMeeting = { hasEnded ->
                    if (hasEnded) {
                        navController.popBackStack(Screen.CategorySelection.route, inclusive = false)
                    } else {
                        navController.navigate(Screen.LeftSession.createRoute(role, code)) {
                            popUpTo(Screen.CategorySelection.route)
                        }
                    }
                }
            )
        }

        // Recordings and settings share the Streamer screen's view model.
        composable(route = Screen.Recordings.route) {
            val streamerEntry = remember(it) { navController.getBackStackEntry(Screen.Streamer.route) }
            RecordingsScreen(
                viewModel = viewModel(streamerEntry),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.StreamerSettings.route) {
            val streamerEntry = remember(it) { navController.getBackStackEntry(Screen.Streamer.route) }
            StreamerSettingsScreen(
                viewModel = viewModel(streamerEntry),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── 12. RELAY DESTINATION WORKFLOW ────────────────────────────────────
        composable(
            route = Screen.Relay.route
        ) {
            RelayScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
