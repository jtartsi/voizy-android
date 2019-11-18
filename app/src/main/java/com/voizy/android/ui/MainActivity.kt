package com.voizy.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.ui.fragment.LibraryFragment
import com.voizy.android.ui.fragment.RecordingFragment
import timber.log.Timber

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.d("file-upload intent $intent")
        Timber.d("file-upload intent extras ${intent.extras}")

        if (intent.action == Intent.ACTION_SEND) {
            val recordingFragment = RecordingFragment()

            val bundle = Bundle()
            bundle.putString(VoizyApp.KEY_ACTION, Intent.ACTION_SEND)
            bundle.putParcelable(VoizyApp.KEY_DATA, intent.clipData)

            recordingFragment.arguments = bundle

            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, recordingFragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, LibraryFragment())
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
