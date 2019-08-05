package com.voizy.android.utils

import io.reactivex.functions.BiFunction

fun <F, S> toPair(): BiFunction<F, S, Pair<F, S>> =
    BiFunction { first, second -> Pair(first, second) }