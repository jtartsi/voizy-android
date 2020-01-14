package com.voizy.android.viewmodels

import android.net.Uri
import com.voizy.android.middleware.local.LocalFileManager
import com.voizy.android.utils.withErrorHandling
import io.reactivex.Observable

class MainActivityViewModel(
    private val localFileManager: LocalFileManager
) :
    DisposingViewModel() {

    companion object {
        private val TAG = MainActivityViewModel::class.java.simpleName
    }

    fun saveImportedFile(uri: Uri): Observable<String> {
        return Observable
            .defer {
                Observable.just(uri)
                    .map {
                        localFileManager.saveUriContentToFile(
                            uri,
                            localFileManager.getImportFilePath()
                        )
                    }
            }
            .withErrorHandling(TAG, "Failed to save received file")
    }
}