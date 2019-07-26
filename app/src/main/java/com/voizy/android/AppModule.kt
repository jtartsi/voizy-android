package com.voizy.android

import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.repositories.FileRepository
import com.voizy.android.viewmodels.MainFragmentViewModel
import com.voizy.android.viewmodels.RecordButtonViewModel
import com.voizy.android.viewmodels.RecordingOverlayViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val factoryModule = module {
    factory { VoizyPlayer() }
}

val singletonModule = module {
    single { VoizyRecorder() }
    single { FileRepository(get()) }
}

val viewModels = module {
    viewModel { MainFragmentViewModel(get(), get()) }
    viewModel { RecordingOverlayViewModel(get(), get(), get()) }
    viewModel { RecordButtonViewModel(get(), get()) }
}

val allModules = listOf(factoryModule, singletonModule, viewModels)