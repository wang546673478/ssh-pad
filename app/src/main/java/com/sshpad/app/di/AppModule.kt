package com.sshpad.app.di

import androidx.lifecycle.SavedStateHandle
import com.sshpad.app.data.repository.SSHConnectionRepository
import com.sshpad.app.data.repository.impl.SSHConnectionRepositoryImpl
import com.sshpad.app.domain.usecase.ConnectToServerUseCase
import com.sshpad.app.domain.usecase.CreateSSHConnectionUseCase
import com.sshpad.app.domain.usecase.DeleteSSHConnectionUseCase
import com.sshpad.app.domain.usecase.GetRecentConnectionsUseCase
import com.sshpad.app.domain.usecase.GetSSHConnectionsUseCase
import com.sshpad.app.domain.usecase.UpdateLastConnectedAtUseCase
import com.sshpad.app.presentation.viewmodel.ConnectionEditViewModel
import com.sshpad.app.presentation.viewmodel.ConnectionListViewModel
import com.sshpad.app.presentation.viewmodel.SSHConnectionViewModel
import com.sshpad.app.presentation.viewmodel.TerminalViewModel
import com.sshpad.app.security.SecureStorage
import com.sshpad.app.ssh.SSHClientWrapper
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module
 * 
 * Week 6 Update:
 * - Added SecureStorage for encrypted credential storage
 * - Added Use Cases for Clean Architecture
 * - Added ViewModels with StateFlow state management
 */
val appModule = module {
    // Security - SecureStorage (singleton)
    single { SecureStorage(androidContext()) }
    
    // Repository - Inject SecureStorage for credential management
    single<SSHConnectionRepository> { 
        SSHConnectionRepositoryImpl(
            context = androidContext(),
            secureStorage = get()
        ) 
    }
    
    // SSH Client (singleton)
    single { SSHClientWrapper(androidContext()) }
    
    // Use Cases - Domain Layer
    single { GetSSHConnectionsUseCase(get()) }
    single { CreateSSHConnectionUseCase(get()) }
    single { DeleteSSHConnectionUseCase(get()) }
    single { ConnectToServerUseCase(get(), get()) }
    single { UpdateLastConnectedAtUseCase(get()) }
    
    // ViewModels - Presentation Layer
    viewModel { 
        ConnectionListViewModel(
            getSSHConnectionsUseCase = get(),
            getRecentConnectionsUseCase = get(),
            createSSHConnectionUseCase = get(),
            deleteSSHConnectionUseCase = get(),
            updateLastConnectedAtUseCase = get()
        ) 
    }
    
    viewModel { 
        ConnectionEditViewModel(
            savedStateHandle = get<SavedStateHandle>(),
            getSSHConnectionsUseCase = get(),
            createSSHConnectionUseCase = get()
        ) 
    }
    
    viewModel { 
        SSHConnectionViewModel(
            getSSHConnectionsUseCase = get(),
            createSSHConnectionUseCase = get(),
            deleteSSHConnectionUseCase = get(),
            connectToServerUseCase = get()
        )
    }
    
    viewModel { 
        TerminalViewModel(
            sshClientWrapper = get(),
            connectToServerUseCase = get(),
            repository = get()
        ) 
    }
}
