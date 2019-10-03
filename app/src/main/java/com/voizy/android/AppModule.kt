package com.voizy.android

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.voizy.android.audio.VoizyPlayer
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.collections.VoizysCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.viewmodels.MainFragmentViewModel
import com.voizy.android.viewmodels.RecordPlayButtonViewModel
import com.voizy.android.viewmodels.RecordingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appLogicsModule = module {
    factory { VoizyPlayer() }
    single { VoizyRecorder() }
}

val repositoryModule = module {
    single { VoizyFirebaseStorage(get()) }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance().reference }
    single { FirebaseAnalytics.getInstance(get()) }
    single { VoizyFirebaseAnalytics(get()) }
    single { VoizysCollection(get()) }
    single { VoizySearchRequestCollection(get()) }
    single { VoizyRepository(get(), get(), get(), get()) }
    single { LocalFileManager(get()) }
}

val viewModels = module {
    viewModel { MainFragmentViewModel(get(), get(), get()) }
    viewModel { RecordingViewModel(get(), get()) }
    viewModel { RecordPlayButtonViewModel(get(), get(), get(), get()) }
}

val allModules = listOf(appLogicsModule, repositoryModule, viewModels)