package com.voizy.android.ui.fragment

class BlankFragment : BaseFragment() {

    companion object {
        val TAG = BlankFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }
}