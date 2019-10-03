package com.voizy.android.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.android.lifecycle.autoDisposable
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.ui.models.Voizy
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordingViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.recording_overlay_fragment.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RecordingFragment : BaseFragment() {

    private val viewModel: RecordingViewModel by inject()
    private var timerDisposable: Disposable? = null
    private val backPressEvent = PublishSubject.create<String>()
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics = get()

    companion object {
        public val TAG = RecordingFragment::class.java.simpleName
        private const val ACCEPT_BACK_THRESHOLD = 3000
    }

    override fun doubleBackPressNeeded(): Boolean {
        return true
    }

    override fun getBackstackTag(): String {
        return TAG
    }

    override fun onBackPressed() {
        backPressEvent.onNext(TAG)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recording_overlay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_save_voizy.setOnClickListener {
            val voizyName = et_voizy_name.text.toString()

            if (!voizyName.isNullOrEmpty()) {
                val voizyToSave = Voizy(
                    name = et_voizy_name.text.toString(),
                    tags = et_voizy_tags.getTags()
                )
                viewModel.saveVoizy(voizyToSave)
                hideSoftKeyboard(view)
                fragmentManager!!.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } else {
                Snackbar.make(
                    view, getString(R.string.voizy_save_failed), Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        startTimer()

        backPressEvent
            .debounce(100, TimeUnit.MILLISECONDS)
            .timeInterval(TimeUnit.MILLISECONDS)
            .filter {
                if (it.time() < ACCEPT_BACK_THRESHOLD) {
                    true
                } else {
                    Snackbar.make(
                        this.view!!,
                        resources.getText(R.string.press_back_again_discard_voizy),
                        Snackbar.LENGTH_LONG
                    ).show()
                    false
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe {
                voizyFirebaseAnalytics.logRecordingCancel()
                fragmentManager!!.popBackStackImmediate(
                    it.value(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }

        viewModel.getRecordingEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                when (it) {
                    VoizyRecorder.RecordingEvent.STOP -> {
                        timerDisposable?.let {
                            it.dispose()
                        }
                        et_voizy_name.visibility = View.VISIBLE
                        et_voizy_tags.visibility = View.VISIBLE
                        btn_save_voizy.visibility = View.VISIBLE
                    }
                    VoizyRecorder.RecordingEvent.START_FAILED -> {
                        Timber.e("Failed to start recording")
                    }
                    VoizyRecorder.RecordingEvent.STOP_FAILED -> {
                        Timber.e("Failed to close recording")
                    }
                    else -> {
                    }
                }
            }
    }

    private fun startTimer() {
        tv_recording_time.text = "00:00 / 00:15"
        timerDisposable = Observable.intervalRange(
            1L, 15, 1L, 1L,
            TimeUnit.SECONDS, AndroidSchedulers.mainThread()
        )
            .map {
                if (it < 10) {
                    "0$it"
                } else {
                    it.toString()
                }
            }
            .map { "00:$it / 00:15" }
            .autoDisposable(getScopeProvider())
            .subscribe {
                tv_recording_time.text = it
            }
    }

    private fun hideSoftKeyboard(view: View) {
        val inputMethodManager: InputMethodManager = context!!
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.rootView.windowToken, 0, null)
    }
}