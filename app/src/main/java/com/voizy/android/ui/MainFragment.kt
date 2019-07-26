package com.voizy.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

    private val viewModel: MainFragmentViewModel by inject<MainFragmentViewModel>()

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
        if (requestCode == 200 && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
            Timber.d("file-iss read permission given")
            viewModel.searchAllVoizys()
        } else {
            Timber.d("file-iss read permission NOT given")
        }
    }

    override fun onStart() {
        super.onStart()

        Completable
            .fromAction {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    200
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

    private fun hasReadFileSystemPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}