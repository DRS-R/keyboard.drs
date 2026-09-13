package com.example

import android.app.Application
import com.example.data.OmniDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OmniApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { OmniDatabase.getDatabase(this, applicationScope) }

    companion object {
        lateinit var instance: OmniApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
