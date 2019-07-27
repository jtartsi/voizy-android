package com.voizy.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.MainFragmentViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainFragment : Fragment() {

    // TODO
    /*
     -done- 1. check if file save (rename) works
     2. fix so that the file save is not allowed to finish until
     -done- 3. fix playback
     4. Showing buttons and edit text in preview quickly, fix this. Could the issue come from BehaviorSubject
     -done- 5. FileName issues
        -done- 5.1. Voizys doesn't have full path
        -done- 5.2. Check FileExtension sitation
    -done- 6. Haptic feedback happens sometime after the recording (maybe 15s timer is not reseted?)
    7. Snackbar for sharing is never shown
    -done- 8. VoizyRecorder crashes after multiple recording attempts
    -done- 9. Recording fragment reset
    10. Change the color of permission request windows
    11. Show recorded Voizys in MainFragment
    12. Update voizy listing after
    13. Play Voizys in MainFragment
    14. Delete Voizy
    15. Share Voizy


     /data/user/0/com.voizy.android/files/Voizy_tmp
     */

    private val viewModel: MainFragmentViewModel by inject<MainFragmentViewModel>()

    companion object {
        private const val REQUEST_READ_EXTERNAL_PERMISSIONS = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_EXTERNAL_PERMISSIONS &&
            permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Timber.d("READ_EXTERNAL_PERMISSION YES")
            viewModel.getReceivedVoizys()
        } else {
            Timber.d("READ_EXTERNAL_PERMISSION NO")
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.getOwnVoizys()

        Completable
            .fromAction {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_PERMISSIONS
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe()

        viewModel.getVoizyStream()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                it.forEach {
                    Timber.d("vzy-list ${it.name} ${it.filePath}")
                }
            }
    }
}