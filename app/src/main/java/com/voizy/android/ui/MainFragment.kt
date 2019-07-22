package com.voizee.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.voizee.android.R
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_new.setOnClickListener {
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragment_container, AudioRecordingFragment(), AudioRecordingFragment.TAG)
                .addToBackStack(AudioRecordingFragment.TAG)
                .commit()
        }
    }
}