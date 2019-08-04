package com.voizy.android

import com.google.firebase.firestore.FirebaseFirestore
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.firebase.collections.VoizyCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.viewmodels.MainFragmentViewModel
import com.voizy.android.viewmodels.RecordButtonViewModel
import com.voizy.android.viewmodels.RecordingOverlayViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appLogicsModule = module {
    factory { VoizyPlayer() }
    single { VoizyRecorder() }
}

val repositoryModule = module {
    single { FirebaseFirestore.getInstance() }
    single { VoizyCollection(get()) }
    single { VoizyRepository(get(), get()) }
    single { LocalFileManager(get()) }
}

val viewModels = module {
    viewModel { MainFragmentViewModel(get(), get()) }
    viewModel { RecordingOverlayViewModel(get(), get(), get()) }
    viewModel { RecordButtonViewModel(get(), get()) }
}

val allModules = listOf(appLogicsModule, repositoryModule, viewModels)