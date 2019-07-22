package com.voizy.android

import com.voizy.android.viewmodels.AudioRecorderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModels = module {
    viewModel { AudioRecorderViewModel() }
}

val allModules = listOf(viewModels)