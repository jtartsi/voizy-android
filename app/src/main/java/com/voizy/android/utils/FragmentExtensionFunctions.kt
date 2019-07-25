package com.voizy.android.utils

import androidx.fragment.app.Fragment
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider

fun Fragment.getScopeProvider(): AndroidLifecycleScopeProvider {
    return AndroidLifecycleScopeProvider.from(viewLifecycleOwner)
}
