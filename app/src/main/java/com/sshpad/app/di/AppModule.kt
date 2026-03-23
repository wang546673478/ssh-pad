package com.sshpad.app.di

import com.sshpad.app.data.repository.SSHConnectionRepository
import com.sshpad.app.data.repository.impl.SSHConnectionRepositoryImpl
import com.sshpad.app.ssh.SSHClientWrapper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin dependency injection module
 */
val appModule = module {
    // Repository
    single<SSHConnectionRepository> { SSHConnectionRepositoryImpl(androidContext()) }
    
    // SSH Client (singleton)
    single { SSHClientWrapper() }
    
    // ViewModels will be added in Week 6
}
