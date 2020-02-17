package com.voizy.android.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.voizy.android.R
import com.voizy.android.ui.fragment.BlankFragment
import com.voizy.android.ui.fragment.RecordVoizyFragment
import com.voizy.android.ui.fragment.YoutubeDownloadFragment

class CreateOptionsAdapter(
    private val context: Context,
    fragmentManger: FragmentManager
) : FragmentPagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private lateinit var recordFragment: RecordVoizyFragment
    private lateinit var youtubeDownloadFragment: YoutubeDownloadFragment

    init {
        recordFragment = RecordVoizyFragment()
        youtubeDownloadFragment = YoutubeDownloadFragment()
    }

    enum class CreateOptions(val value: Int) {
        FILE(0),
        RECORD(1),
        DOWNLOAD(2)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            CreateOptions.FILE.value -> {
                // Timber.d("options-pager getItem() $position return file") // TODO pager-todo clean this file
                return context.getString(R.string.file)
            }
            CreateOptions.RECORD.value -> {
                // Timber.d("options-pager getItem() $position return record")
                return context.getString(R.string.record)
            }
            CreateOptions.DOWNLOAD.value -> {
                // Timber.d("options-pager getItem() $position return download")
                return context.getString(R.string.download)
            }
            else -> {
                // Timber.d("options-pager getItem() $position return other")
                return context.getString(R.string.record)
            }
        }
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            CreateOptions.FILE.value -> {
                // Timber.d("options-pager getItem() $position return file")
                return BlankFragment()
            }
            CreateOptions.RECORD.value -> {
                // Timber.d("options-pager getItem() $position return record")
                return RecordVoizyFragment()
            }
            CreateOptions.DOWNLOAD.value -> {
                // Timber.d("options-pager getItem() $position return download")
                return YoutubeDownloadFragment()
            }
            else -> {
                // Timber.d("options-pager getItem() $position return other")
                return RecordVoizyFragment()
            }
        }
    }

    override fun getCount(): Int {
        return CreateOptions.values().size
    }
}