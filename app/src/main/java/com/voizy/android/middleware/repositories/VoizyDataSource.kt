package com.voizy.android.middleware.repositories

import androidx.paging.PageKeyedDataSource
import com.voizy.android.middleware.firebase.collections.VoizySearchRequestCollection
import com.voizy.android.middleware.firebase.models.FirestoreVoizySearchRequest.SortOrder
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.utils.NetworkState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class VoizyDataSource(
    private val searchKeyword: String,
    private val sortOrder: SortOrder,
    private val compositeDisposable: CompositeDisposable,
    private val searchCollection: VoizySearchRequestCollection
) : PageKeyedDataSource<Int, Voizy>() {

    val networkState = PublishSubject.create<NetworkState>()
    val initialLoading = PublishSubject.create<NetworkState>()

    companion object {
        private val TAG = VoizyDataSource::class.java.simpleName
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Voizy>
    ) {
        Timber.d("pagination loadInitial page, count ${params.requestedLoadSize}")
        initialLoading.onNext(NetworkState.LOADING)
        networkState.onNext(NetworkState.LOADING)

        val disposable = searchCollection.voizys(
            searchKeyword,
            sortOrder,
            0,
            params.requestedLoadSize
        ).subscribe(
            {
                Timber.d(
                    "pagination loadInitial result.from ${it.resultsInfo.from}, itemsCount ${it.resultsInfo.itemsCount}" +
                        ", hasMore ${it.resultsInfo.hasMore}, totalCount ${it.resultsInfo.totalCount}"
                )
                val nextKey = if (it.resultsInfo.hasMore) {
                    params.requestedLoadSize
                } else {
                    null
                }
                callback.onResult(it.items, null, nextKey)
                initialLoading.onNext(NetworkState.LOADED)
                networkState.onNext(NetworkState.LOADED)
            },
            {
                Timber.e(it, "Loading voizys failed")
                initialLoading.onNext(NetworkState.FAILED)
                networkState.onNext(NetworkState.FAILED)
            })
        compositeDisposable.add(disposable)
    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, Voizy>
    ) {
        Timber.d("pagination loadAfter requested from ${params.key}, pageSize ${params.requestedLoadSize}")
        Timber.i("Loading page ${params.key}, count ${params.requestedLoadSize}")
        networkState.onNext(NetworkState.LOADING)

        val disposable = searchCollection.voizys(
            searchKeyword,
            sortOrder,
            params.key,
            params.requestedLoadSize
        ).subscribe(
            {
                Timber.d(
                    "pagination loadAfter result.from ${it.resultsInfo.from}, itemsCount ${it.resultsInfo.itemsCount}" +
                        ", hasMore ${it.resultsInfo.hasMore}, totalCount ${it.resultsInfo.totalCount}"
                )

                Timber.i("pagination loadAfter result.from ${it.resultsInfo.from}")
                val nextKey = if (it.resultsInfo.hasMore) {
                    (params.key + params.requestedLoadSize)
                } else {
                    null
                }
                callback.onResult(it.items, nextKey)
                networkState.onNext(NetworkState.LOADED)
            },
            {
                Timber.e(it, "Loading voizys failed")
                networkState.onNext(NetworkState.FAILED)
            })
        compositeDisposable.add(disposable)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Voizy>) {
    }
}