package com.voizy.android.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.voizy.android.R
import com.voizy.android.ui.fragment.BaseFragment
import com.voizy.android.ui.fragment.FileImportFragment
import com.voizy.android.ui.fragment.RecordVoizyFragment
import com.voizy.android.ui.fragment.YoutubeDownloadFragment

class CreateOptionsAdapter(
    private val context: Context,
    fragmentManger: FragmentManager
) : FragmentPagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val fragments: List<BaseFragment> = listOf(
        FileImportFragment(),
        RecordVoizyFragment(),
        YoutubeDownloadFragment()
    )

    enum class CreateOptions(val value: Int) {
        FILE(0),
        RECORD(1),
        DOWNLOAD(2)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            CreateOptions.FILE.value -> context.getString(R.string.file)
            CreateOptions.RECORD.value -> context.getString(R.string.record)
            CreateOptions.DOWNLOAD.value -> context.getString(R.string.download)
            else -> context.getString(R.string.record)
        }
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return CreateOptions.values().size
    }
}