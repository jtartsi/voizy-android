package com.voizy.android.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.voizy.android.ui.fragment.FileImportFragment
import com.voizy.android.ui.fragment.RecordVoizyFragment
import com.voizy.android.ui.fragment.TabFragment
import com.voizy.android.ui.fragment.YoutubeDownloadFragment

class CreateOptionsAdapter(
    fragmentManger: FragmentManager
) : FragmentPagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments: List<TabFragment> = listOf(
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
        return fragments[position].getTabTitle()
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return CreateOptions.values().size
    }
}