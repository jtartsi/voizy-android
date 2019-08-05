package com.voizy.android.utils

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.reactivex.Observable

fun <T> Task<T>.toObservable(): Observable<T> {
    return Observable.fromCallable {
        Tasks.await(this)
    }
}
