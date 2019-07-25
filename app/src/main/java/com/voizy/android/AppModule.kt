package com.voizy.android

import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.viewmodels.RecordingOverlayViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val singletonModule = module {
    single { VoizyRecorder() }
}

val viewModels = module {
    viewModel { RecordingOverlayViewModel(get(), get()) }
}

val allModules = listOf(viewModels, singletonModule)