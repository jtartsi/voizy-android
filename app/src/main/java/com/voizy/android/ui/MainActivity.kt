package com.voizy.android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.ui.fragment.AudioClipFragment
import com.voizy.android.ui.fragment.LibraryFragment
import com.voizy.android.viewmodels.MainActivityViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by inject()

    companion object {
        const val PICK_FILE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.VoizyTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, LibraryFragment(), LibraryFragment.TAG)
            .commit()

        if (intent.action == Intent.ACTION_SEND) {
            val uri = intent.clipData?.getItemAt(0)?.uri
            saveImportedFile(uri)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_FILE_REQUEST_CODE) {
            saveImportedFile(data!!.data)
        }
    }

    private fun saveImportedFile(uri: Uri?) {
        uri?.let { uri ->
            viewModel.saveImportedFile(uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(AndroidLifecycleScopeProvider.from(this))
                .subscribe(openFileConsumer())
        }
    }

    private fun openFileConsumer(): Consumer<String> {
        return Consumer { filePath ->
            val bundle = Bundle()
            bundle.putSerializable(VoizyApp.KEY_DATA, filePath)

            val audioClipFragment = AudioClipFragment()
            audioClipFragment.arguments = bundle

            val createOptionsFragment =
                supportFragmentManager.findFragmentById(R.id.record_button_fragment)

            supportFragmentManager.beginTransaction()
                .hide(createOptionsFragment!!)
                .replace(R.id.fragment_container, audioClipFragment)
                .addToBackStack(AudioClipFragment.TAG)
                .commit()
        }
    }
}
