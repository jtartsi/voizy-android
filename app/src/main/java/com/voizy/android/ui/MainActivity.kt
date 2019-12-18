package com.voizy.android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.ui.fragment.LibraryFragment
import com.voizy.android.ui.fragment.RecordingFragment
import timber.log.Timber

class MainActivity : BaseActivity() {

    companion object {
        const val PICK_FILE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.VoizyTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDataImport()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_FILE_REQUEST_CODE) {
            Timber.d("data-import onActivityResult() data $data")
            Timber.d("data-import onActivityResult() data.data ${data!!.data}")
            Timber.d("data-import onActivityResult() clipData ${data!!.clipData}")
            Timber.d("data-import onActivityResult() dataString ${data!!.dataString}")
            Timber.d("data-import onActivityResult() extras ${data!!.extras}")
            openFileInApp(data!!.data)
        }
    }

    private fun initDataImport() {
        if (intent.action == Intent.ACTION_SEND) {
            val uri = intent.clipData?.getItemAt(0)?.uri
            openFileInApp(uri)
        } else {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, LibraryFragment())
                .addToBackStack(LibraryFragment.TAG)
                .commit()
        }
    }

    private fun openFileInApp(uri: Uri?) {
        val bundle = Bundle()
        bundle.putString(VoizyApp.KEY_ACTION, Intent.ACTION_SEND)
        bundle.putParcelable(VoizyApp.KEY_DATA, uri)

        val recordingFragment = RecordingFragment()
        recordingFragment.arguments = bundle

        val createOptionsFragment =
            supportFragmentManager.findFragmentById(R.id.record_button_fragment)

        supportFragmentManager.beginTransaction()
            .hide(createOptionsFragment!!)
            .add(R.id.fragment_container, recordingFragment)
            .addToBackStack(RecordingFragment.TAG)
            .commit()
    }
}
