package com.voizy.android.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordingViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Timed
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.recording_overlay_fragment.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

class RecordingFragment : BaseFragment() {

    private val viewModel: RecordingViewModel by inject()
    private var timerDisposable: Disposable? = null
    private val backPressEvent = PublishSubject.create<String>()
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics = get()

    // TODO cleaning move analytics to ViewModel
    // TODO cleaning use RxClicks and RxView updates
    // TODO cleaning get data for specific views
    // TODO cleaning move data validation etc. logic to ViewModel side.

    companion object {
        public val TAG = RecordingFragment::class.java.simpleName
        private const val ACCEPT_BACK_THRESHOLD = 3000
    }

    override fun doubleBackPressNeeded(): Boolean {
        return true
    }

    override fun getFragmentTag(): String {
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

            val locale = Locale.getDefault()

            Timber.d("save-voizy onClick()")
            if (!voizyName.isNullOrEmpty()) {
                Timber.d("save-voizy onClick() name ok")
                val voizyToSave = Voizy(
                    name = et_voizy_name.text.toString(),
                    tags = et_voizy_tags.getTags(),
                    locale = locale.toString(),
                    localeLang = locale.language,
                    localeCountry = locale.country
                )

                Timber.d("save-voizy voizy $voizyToSave")
                viewModel.saveVoizy(voizyToSave)
                Timber.d("save-voizy viewModel called")

                hideSoftKeyboard(view)
                Timber.d("save-voizy hidesoftKeyboard")
                fragmentManager!!.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                Timber.d("save-voizy popBackstack")
            } else {
                Timber.d("save-voizy else")
                Snackbar.make(
                    view, getString(R.string.voizy_save_failed), Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        Timber.d("file-upload arguments $arguments")
    }

    override fun onStart() {
        super.onStart()

        if (isFileSendAction()) {
            showSaveLayout()

            val fileUri = arguments!!.get(VoizyApp.KEY_DATA) as Uri
            viewModel.saveReceivedFileToTempLocation(fileUri)
                .switchMap { viewModel.getAudioFileLengthInSeconds(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(getScopeProvider())
                .subscribe {
                    showTimeText(it)
                }
        } else {
            startTimer()
        }

        setObservables()
    }

    private fun setObservables() {
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
            .autoDisposable(getScopeProvider())
            .subscribe(backPressConsumer())

        viewModel.getSaveVoizyEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(saveEventConsumer())

        viewModel.getRecordingEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(recordingEventConsumer())
    }

    private fun backPressConsumer(): Consumer<Timed<String>> {
        return Consumer {
            if (isFileSendAction()) {
                this.activity!!.finish()
            } else {
                voizyFirebaseAnalytics.logRecordingCancel()
                fragmentManager!!.popBackStackImmediate(
                    it.value(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
        }
    }

    private fun saveEventConsumer(): Consumer<Pair<Boolean, Voizy?>> {
        return Consumer {
            if (it.first) {
                Snackbar.make(
                    view!!, getString(R.string.voizy_created_share), Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    view!!,
                    getString(R.string.voizy_save_failed),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun recordingEventConsumer(): Consumer<VoizyRecorder.RecordingEvent> {
        return Consumer {
            when (it) {
                VoizyRecorder.RecordingEvent.STOP -> {
                    timerDisposable?.let {
                        it.dispose()
                    }
                    showSaveLayout()
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
        showTimeText(0)
        timerDisposable = Observable.intervalRange(
            1L, 15, 1L, 1L,
            TimeUnit.SECONDS, AndroidSchedulers.mainThread()
        )
            .autoDisposable(getScopeProvider())
            .subscribe {
                showTimeText(it.toInt())
            }
    }

    private fun showTimeText(timeInSeconds: Int) {
        val secondsString = if (timeInSeconds < 10) {
            "0$timeInSeconds"
        } else {
            timeInSeconds.toString()
        }
        tv_recording_time.text = "00:".plus(secondsString).plus(" / 00:15")
    }

    private fun isFileSendAction(): Boolean {
        return arguments != null &&
            arguments!!.get(VoizyApp.KEY_ACTION) == Intent.ACTION_SEND
    }

    private fun showSaveLayout() {
        et_voizy_name.visibility = View.VISIBLE
        et_voizy_tags.visibility = View.VISIBLE
        btn_save_voizy.visibility = View.VISIBLE
    }

    private fun hideSoftKeyboard(view: View) {
        val inputMethodManager: InputMethodManager = context!!
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.rootView.windowToken, 0, null)
    }
}