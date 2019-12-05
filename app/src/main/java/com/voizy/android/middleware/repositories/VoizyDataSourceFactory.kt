package com.voizy.android.middleware.repositories

import androidx.paging.DataSource
import androidx.paging.DataSource.Factory
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.models.Voizy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class VoizyDataSourceFactory(
    private val searchKeyword: String,
    private val compositeDisposable: CompositeDisposable,
    private val searchCollection: VoizySearchRequestCollection
) : Factory<Int, Voizy>() {

    val dataSource = PublishSubject.create<VoizyDataSource>()

    override fun create(): DataSource<Int, Voizy> {
        val voizysDataSource =
            VoizyDataSource(
                searchKeyword,
                compositeDisposable,
                searchCollection
            )
        dataSource.onNext(voizysDataSource)
        return voizysDataSource
    }
}