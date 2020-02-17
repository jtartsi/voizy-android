package com.voizy.android.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.voizy.android.ui.fragment.RecordVoizyFragment
import com.voizy.android.ui.fragment.YoutubeDownloadFragment
import timber.log.Timber

class CreateOptionsAdapter(
    fragmentManger: FragmentManager
) : FragmentPagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private lateinit var recordFragment: RecordVoizyFragment
    private lateinit var youtubeDownloadFragment: YoutubeDownloadFragment

    init {
        recordFragment = RecordVoizyFragment()
        youtubeDownloadFragment = YoutubeDownloadFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                Timber.d("options-pager getItem() $position return file")
                return "File"
            }
            1 -> {
                Timber.d("options-pager getItem() $position return record")
                return "Record"
            }
            2 -> {
                Timber.d("options-pager getItem() $position return download")
                return "Download"
            }
            else -> {
                Timber.d("options-pager getItem() $position return other")
                return "Record"
            }
        }
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                Timber.d("options-pager getItem() $position return file")
                return RecordVoizyFragment()
            }
            1 -> {
                Timber.d("options-pager getItem() $position return record")
                return RecordVoizyFragment()
            }
            2 -> {
                Timber.d("options-pager getItem() $position return download")
                return YoutubeDownloadFragment()
            }
            else -> {
                Timber.d("options-pager getItem() $position return other")
                return RecordVoizyFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }
}