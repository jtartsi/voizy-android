package com.voizy.android.ui.fragment

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    open fun doubleBackPress(): Boolean {
        return false
    }

    abstract fun getBackstackTag(): String
}