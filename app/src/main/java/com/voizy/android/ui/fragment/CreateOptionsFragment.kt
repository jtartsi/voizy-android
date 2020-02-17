package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.voizy.android.R
import com.voizy.android.ui.adapter.CreateOptionsAdapter
import kotlinx.android.synthetic.main.create_options_layout.*

class CreateOptionsFragment : BaseFragment() {

    private lateinit var optionsAdapter: CreateOptionsAdapter

    companion object {
        val TAG = CreateOptionsFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_options_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        optionsAdapter = CreateOptionsAdapter(fragmentManager!!)
        pager_create_voizy.adapter = optionsAdapter
        tabs_create_voizy.setupWithViewPager(pager_create_voizy)
    }

    override fun onStart() {
        super.onStart()
    }
}