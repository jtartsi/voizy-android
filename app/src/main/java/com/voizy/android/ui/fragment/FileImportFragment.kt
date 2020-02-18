package com.voizy.android.ui.fragment

import com.voizy.android.R

class FileImportFragment : TabFragment() {
    
    override fun getTabTitle(): String {
        return getString(R.string.file)
    }

    companion object {
        val TAG = FileImportFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }
}