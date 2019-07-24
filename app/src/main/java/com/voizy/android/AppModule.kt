package com.voizy.android

import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.viewmodels.RecordingOverlayViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val factoryModules = module {
    factory { VoizyRecorder() }
}

val viewModels = module {
    viewModel { RecordingOverlayViewModel(get()) }
}

val allModules = listOf(viewModels, factoryModules)