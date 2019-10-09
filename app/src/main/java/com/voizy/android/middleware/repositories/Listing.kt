package com.voizy.android.middleware.repositories

import androidx.paging.PagedList
import com.voizy.android.utils.NetworkState
import io.reactivex.Observable

data class Listing<T>(
    val pagedListObservable: Observable<PagedList<T>>,
    val networkSate: Observable<NetworkState>,
    val initialLoading: Observable<NetworkState>
)