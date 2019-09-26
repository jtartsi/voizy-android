package com.voizy.android.utils

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import timber.log.Timber

fun <T> Task<T>.toObservable(): Observable<T> {
    return Observable.fromCallable {
        Tasks.await(this)
    }
}

fun Observable<DocumentReference>.snapshotChange(): Observable<DocumentSnapshot> {
    return this.flatMap { documentRef ->
        Observable.create<DocumentSnapshot> { emitter ->
            documentRef.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                Timber.d("snapshotChange")
                if (documentSnapshot != null && firebaseFirestoreException == null) {
                    emitter.onNext(documentSnapshot)
                } else {
                    emitter.onError(Throwable(firebaseFirestoreException))
                }
            }
        }
    }
}

fun Observable<CollectionReference>.collectionChange(): Observable<List<DocumentSnapshot>> {
    return this.flatMap { collectionRef ->
        Observable.create<List<DocumentSnapshot>> { emitter ->
            collectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot != null && firebaseFirestoreException == null) {
                    emitter.onNext(querySnapshot.documents)
                } else {
                    emitter.onError(Throwable(firebaseFirestoreException))
                }
            }
        }
    }
}

fun <F, S> toPair(): BiFunction<F, S, Pair<F, S>> =
    BiFunction { first, second -> Pair(first, second) }

fun <T> Observable<T>.withErrorHandling(errorHandling: (throwable: Throwable) -> Observable<T>): Observable<T> {
    return this.onErrorResumeNext { throwable: Throwable ->
        errorHandling(throwable)
    }
}

fun <T> Flowable<T>.withErrorHandling(errorHandling: (throwable: Throwable) -> Flowable<T>): Flowable<T> {
    return this.onErrorResumeNext { throwable: Throwable ->
        errorHandling(throwable)
    }
}

fun <T> Single<T>.withErrorHandling(errorHandling: (throwable: Throwable) -> Single<T>): Single<T> {
    return this.onErrorResumeNext { throwable: Throwable ->
        errorHandling(throwable)
    }
}

fun <T> Observable<T>.withErrorHandling(
    tag: String,
    message: String,
    defaultValueOnError: Observable<T> = Observable.empty()
): Observable<T> {
    return this.withErrorHandling {
        Timber.e(it, "$tag $message")
        defaultValueOnError
    }
}

fun <T> Flowable<T>.withErrorHandling(
    tag: String,
    message: String,
    defaultValueOnError: Flowable<T> = Flowable.empty()
): Flowable<T> {
    return this.withErrorHandling {
        Timber.e(it, "$tag $message")
        defaultValueOnError
    }
}

fun <T> Flowable<T>.withErrorHandling(tag: String, message: String): Flowable<T> {
    return this.withErrorHandling {
        Timber.e(it, "$tag $message")
        Flowable.empty()
    }
}

fun <T> Single<T>.withErrorHandling(tag: String, message: String, valueOnError: T): Single<T> {
    return this.withErrorHandling {
        Timber.e(it, "$tag $message")
        Single.just(valueOnError)
    }
}