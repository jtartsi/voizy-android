package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.voizy.android.R

class CreateVoizyFragment : BaseFragment() {

    companion object {
        val TAG = CreateVoizyFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_voizy_fragment, container, false)
    }
}