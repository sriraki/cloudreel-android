package com.cloudreel.mobile.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Streamer : Screen("streamer")
    object Relay : Screen("relay")
}
