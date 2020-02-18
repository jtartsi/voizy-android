package com.voizy.android.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.tabs.TabLayout
import com.voizy.android.R
import com.voizy.android.ui.MainActivity
import com.voizy.android.ui.adapter.CreateOptionsAdapter
import com.voizy.android.ui.adapter.CreateOptionsAdapter.CreateOptions
import com.voizy.android.utils.SupportedFileTypes
import com.voizy.android.viewmodels.CreateOptionsViewModel
import kotlinx.android.synthetic.main.create_options_layout.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class CreateOptionsFragment : BaseFragment() {

    private lateinit var optionsAdapter: CreateOptionsAdapter
    private val viewModel: CreateOptionsViewModel by inject()

    companion object {
        val TAG = CreateOptionsFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun useCustomBackPress(): Boolean {
        return true
    }

    override fun onBackPressed(): Boolean {
        if (pager_create_voizy.currentItem != 1) { // TODO create-pager change this to RecorVoizy.val or something
            pager_create_voizy.setCurrentItem(1, true)
        } else if (!getCurrentFragmentFromPager().onBackPressed()) {
            fragmentManager!!.popBackStack(
                TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        return true
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
        optionsAdapter = CreateOptionsAdapter(context!!, childFragmentManager)
        pager_create_voizy.adapter = optionsAdapter
        tabs_create_voizy.setupWithViewPager(pager_create_voizy)
        tabs_create_voizy.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                val selectedPosition = tabs_create_voizy.selectedTabPosition
                Timber.d("options-pager onTabSelected $selectedPosition")
                when (selectedPosition) {
                    CreateOptions.FILE.value -> {
                        viewModel.logFileSelected()
                        val pickFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                            putExtra(Intent.EXTRA_MIME_TYPES, SupportedFileTypes.toArray())
                        }
                        activity!!.startActivityForResult(
                            pickFileIntent,
                            MainActivity.PICK_FILE_REQUEST_CODE
                        )
                    }
                    CreateOptions.RECORD.value -> {
                        viewModel.logRecordSelected()
                    }
                    CreateOptions.DOWNLOAD.value -> {
                        viewModel.logDownloadSelected()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        pager_create_voizy.setCurrentItem(
            CreateOptions.RECORD.value, false
        )
    }

    private fun getCurrentFragmentFromPager(): BaseFragment {
        return optionsAdapter.getItem(pager_create_voizy.currentItem) as BaseFragment
    }
}