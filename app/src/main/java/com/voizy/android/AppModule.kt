package com.voizy.android

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.voizy.android.audio.AudioPlayer
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.VoizyFirebaseStorage
import com.voizy.android.middleware.firebase.collections.ShareCollection
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.collections.VoizysCollection
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.middleware.repositories.ShareRepository
import com.voizy.android.middleware.repositories.VoizyRepository
import com.voizy.android.utils.ShareManager
import com.voizy.android.viewmodels.CreateOptionsViewModel
import com.voizy.android.viewmodels.LibraryFragmentViewModel
import com.voizy.android.viewmodels.RecordingViewModel
import io.reactivex.disposables.CompositeDisposable
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appLogicsModule = module {
    factory { CompositeDisposable() }
    factory { ShareManager(get()) }
    single { AudioPlayer() }
    single { AudioRecorder() }
    single { LocalFileManager(get()) }
}

val repositoryModule = module {
    single { VoizyFirebaseStorage(get()) }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance().reference }
    single { FirebaseAnalytics.getInstance(get()) }
    single { VoizyFirebaseAnalytics(get()) }
    single { VoizysCollection(get()) }
    single { VoizySearchRequestCollection(get()) }
    single { ShareCollection(get()) }
    single { VoizyRepository(get(), get(), get(), get(), get(), get()) }
    single { ShareRepository(get(), get()) }
}

val viewModels = module {
    viewModel { LibraryFragmentViewModel(get(), get(), get(), get(), get()) }
    viewModel { RecordingViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { CreateOptionsViewModel(get(), get()) }
}

val allModules = listOf(appLogicsModule, repositoryModule, viewModels)