package com.cloudreel.mobile.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object CategorySelection : Screen("category_selection")
    object LiveSessions : Screen("live_sessions")
    object Broadcast : Screen("broadcast")
    object CellularBondingMesh : Screen("cellular_bonding_mesh")
    object Home : Screen("category_selection")
    object SessionEntry : Screen("session_entry/{mode}") {
        fun createRoute(mode: String) = "session_entry/$mode"
    }
    object HostWorkflow : Screen("host_workflow/{code}") {
        fun createRoute(code: String) = "host_workflow/$code"
    }
    object ParticipantWorkflow : Screen("participant_workflow/{code}") {
        fun createRoute(code: String) = "participant_workflow/$code"
    }
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Streamer : Screen("streamer")
    object Relay : Screen("relay")
}
