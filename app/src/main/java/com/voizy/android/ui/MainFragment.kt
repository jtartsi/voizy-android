package com.voizy.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.MainFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
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

    override fun onStart() {
        super.onStart()

        Timber.d("vzy-list")

        viewModel.getVoizyStream()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                it.forEach {
                    Timber.d("vzy-list ${it.name} ${it.filePath}")
                }
            }

        viewModel.searchAllVoizys()
    }
}