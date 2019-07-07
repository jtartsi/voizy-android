package com.voizee.android

import com.voizee.android.viewmodels.AudioRecorderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModels = module {
    viewModel { AudioRecorderViewModel() }
}

val allModules = listOf(viewModels)