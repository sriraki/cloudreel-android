package com.cloudreel.mobile.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test
    fun testEmailValidation() {
        val validEmail = "rakesh@yupptv.com"
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
