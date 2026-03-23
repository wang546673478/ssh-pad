package com.sshpad.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.sshpad.app.di.appModule

/**
 * SSH Pad Application Class
 * 
 * Initializes dependency injection and app-wide components
 */
class SSHPadApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin for dependency injection
        startKoin {
            androidLogger()
            androidContext(this@SSHPadApplication)
            modules(appModule)
        }
    }
}
