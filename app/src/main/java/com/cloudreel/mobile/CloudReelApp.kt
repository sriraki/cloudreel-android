package com.cloudreel.mobile

import android.app.Application
import com.cloudreel.mobile.data.local.SessionManager
import com.cloudreel.mobile.data.remote.AuthRepository

class CloudReelApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        sessionManager = SessionManager(applicationContext)
        authRepository = AuthRepository(sessionManager = sessionManager)
    }

    companion object {
        lateinit var instance: CloudReelApp
            private set
    }
}
