package com.voizy.android.utils

import android.app.Activity
import com.voizy.android.ui.BaseActivity

fun Activity.showProgressBar(visible: Boolean) {
    if (this is BaseActivity) {
        this.showProgressBar(visible)
    }
}

// fun Activity.getScopeProvider(): AndroidLifecycleScopeProvider {
//     AndroidLifecycleScopeProvider.from(this)
// }
