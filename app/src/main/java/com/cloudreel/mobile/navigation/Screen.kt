package com.cloudreel.mobile.navigation

import android.net.Uri
import com.cloudreel.mobile.ui.streamer.StreamerRole

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object CategorySelection : Screen("category_selection")
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
    object Streamer : Screen("streamer?role={role}&code={code}") {
        fun createRoute(role: StreamerRole = StreamerRole.REPORTER, code: String = "") =
            "streamer?role=${role.name}&code=${Uri.encode(code)}"
    }
    object WaitingLobby : Screen("waiting_lobby?code={code}") {
        fun createRoute(code: String) = "waiting_lobby?code=${Uri.encode(code)}"
    }
    object LeftSession : Screen("left_session?role={role}&code={code}") {
        fun createRoute(role: StreamerRole, code: String) = "left_session?role=${role.name}&code=${Uri.encode(code)}"
    }
    object Recordings : Screen("streamer_recordings")
    object StreamerSettings : Screen("streamer_settings")
    object Relay : Screen("relay")
    object DeviceSetup : Screen("device_setup/{mode}/{code}") {
        fun createRoute(mode: String, code: String) = "device_setup/$mode/$code"
    }
    object ActiveSession : Screen("active_session/{mode}/{code}") {
        fun createRoute(mode: String, code: String) = "active_session/$mode/$code"
    }
}
