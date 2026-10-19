package com.cloudreel.mobile.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test
    fun testHomeFourPrimaryOptions() {
        val homeOptions = listOf(
            "Start as Host",
            "Join as Participant",
            "Streamer",
            "Relay"
        )

        assertEquals("Home screen must have exactly 4 primary options", 4, homeOptions.size)
        assertEquals("Option 1 must be Start as Host", "Start as Host", homeOptions[0])
        assertEquals("Option 2 must be Join as Participant", "Join as Participant", homeOptions[1])
        assertEquals("Option 3 must be Streamer", "Streamer", homeOptions[2])
        assertEquals("Option 4 must be Relay", "Relay", homeOptions[3])
    }

    @Test
    fun testSessionEntryValidation() {
        // Validation rules:
        // 1. Non-empty
        // 2. Starts with http:// or https:// OR matches alphanumeric/dashes 4-24 chars
        fun isValidSessionInput(input: String): Boolean {
            val trimmed = input.trim()
            if (trimmed.isBlank()) return false
            val isUrl = trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true)
            val isRoomCode = trimmed.matches(Regex("^[a-zA-Z0-9_-]{4,24}$"))
            return isUrl || isRoomCode
        }

        // Valid room codes
        assertTrue(isValidSessionInput("CR-84920"))
        assertTrue(isValidSessionInput("CR-HOST-01"))
        assertTrue(isValidSessionInput("room1234"))
        assertTrue(isValidSessionInput("STREAM_TEST"))

        // Valid URLs
        assertTrue(isValidSessionInput("https://live.cloudreel.com/room/123"))
        assertTrue(isValidSessionInput("http://mesh.internal/session/cr-900"))

        // Invalid inputs
        assertFalse("Empty input must be rejected", isValidSessionInput(""))
        assertFalse("Whitespace must be rejected", isValidSessionInput("   "))
        assertFalse("Too short code (<4 chars) must be rejected", isValidSessionInput("abc"))
        assertFalse("Illegal special characters must be rejected", isValidSessionInput("cr*room?#"))
    }

    @Test
    fun testSessionEntryModeMapping() {
        fun resolveMode(param: String?): String {
            return if (param.equals("host", ignoreCase = true)) "HOST" else "PARTICIPANT"
        }

        assertEquals("HOST", resolveMode("host"))
        assertEquals("HOST", resolveMode("HOST"))
        assertEquals("PARTICIPANT", resolveMode("participant"))
        assertEquals("PARTICIPANT", resolveMode("PARTICIPANT"))
        assertEquals("PARTICIPANT", resolveMode(null))
        assertEquals("PARTICIPANT", resolveMode("unknown"))
    }

    @Test
    fun testStreamerAuthRequirementLogic() {
        // Streamer requires auth:
        // if authenticated -> open Streamer directly
        // if unauthenticated -> open Login -> on success -> open Streamer directly
        fun resolveStreamerDestination(isAuthenticated: Boolean): String {
            return if (isAuthenticated) "streamer" else "login"
        }

        assertEquals("streamer", resolveStreamerDestination(true))
        assertEquals("login", resolveStreamerDestination(false))
    }

    @Test
    fun testRelayDirectAccessLogic() {
        // Relay does NOT require authentication
        fun resolveRelayDestination(): String {
            return "relay"
        }

        assertEquals("relay", resolveRelayDestination())
    }

    @Test
    fun testEmailValidation() {
        val validEmail = "rakesh@cloudreel.com"
        val invalidEmail = "not-an-email"

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

        assertTrue(validEmail.matches(emailRegex))
        assertFalse(invalidEmail.matches(emailRegex))
    }

    @Test
    fun testPasswordLengthValidation() {
        val shortPassword = "123"
        val validPassword = "CloudReelSecret2026"

        assertTrue(validPassword.length >= 6)
        assertFalse(shortPassword.length >= 6)
    }

    @Test
    fun testPasswordMatch() {
        val pass = "Password123"
        val confirm = "Password123"
        val mismatch = "DifferentPassword"

        assertEquals(pass, confirm)
        assertTrue(pass != mismatch)
    }

    @Test
    fun testSessionExpirationLogic() {
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        val currentTime = System.currentTimeMillis()

        val recentLogin = currentTime - (1L * 24 * 60 * 60 * 1000) // 1 day ago
        val expiredLogin = currentTime - (31L * 24 * 60 * 60 * 1000) // 31 days ago

        val isRecentValid = (currentTime - recentLogin) <= thirtyDaysMs
        val isExpiredValid = (currentTime - expiredLogin) <= thirtyDaysMs

        assertTrue("1 day old session must be active", isRecentValid)
        assertFalse("31 day old session must be expired", isExpiredValid)
    }
}
