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
import com.jakewharton.rxbinding2.view.RxView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.audio.PlaybackEvent
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.widget.PlayPauseButton
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordingViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Timed
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.recording_fragment.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RecordingFragment : BaseFragment() {
    /*
     TODO code improvements
      - Break into RecordingFragment & SaveFragment
      - SaveFragment could be abstract and then ImportSave, NormalSaveFragment
            could distinguish UI navigation patterns
     */
    private val viewModel: RecordingViewModel by inject()
    private var timerDisposable: Disposable? = null
    private val backPressEvent = PublishSubject.create<String>()
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics = get()

    companion object {
        public val TAG = RecordingFragment::class.java.simpleName
        private const val ACCEPT_BACK_THRESHOLD = 3000
    }

    override fun useCustomBackPress(): Boolean {
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
        return inflater.inflate(R.layout.recording_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayback()
        initSave()
        initFileInput()
        initBackPress()
        initRecordEvents()

        if (!isFileSendAction()) {
            startTimer()
        } else {
            showSaveLayout()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopPlayback()
            .autoDisposable(getScopeProvider())
            .subscribe()
    }

    private fun initPlayback() {
        RxView.clicks(btn_playback)
            .flatMap { viewModel.togglePlay() }
            .autoDisposable(getScopeProvider())
            .subscribe()

        viewModel.getPlaybackEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (it.playbackEvent == PlaybackEvent.START) {
                    btn_playback.state = PlayPauseButton.State.STOP_ICON
                } else if (it.playbackEvent == PlaybackEvent.STOP) {
                    btn_playback.state = PlayPauseButton.State.PLAY_ICON
                }
            }
    }

    private fun initSave() {
        RxView.clicks(btn_save_voizy)
            .map { btn_save_voizy }
            .autoDisposable(getScopeProvider())
            .subscribe(saveClickConsumer())
    }

    private fun saveClickConsumer(): Consumer<View> {
        return Consumer { view ->
            if (!et_voizy_name.text.toString().isNullOrEmpty()) {
                val voizy = getVoizyFromUserInputs()
                viewModel.saveVoizy(voizy)
                hideSoftKeyboard(view)
                fragmentManager!!.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        VoizyDetailsFragment(),
                        VoizyDetailsFragment.TAG
                    )
                    .addToBackStack(VoizyDetailsFragment.TAG)
                    .commit()
            } else {
                Snackbar.make(
                    view, getString(R.string.voizy_save_failed), Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initFileInput() {
        Observable.just(isFileSendAction())
            .filter { it }
            .flatMap {
                val fileUri = arguments!!.get(VoizyApp.KEY_DATA) as Uri
                viewModel.saveReceivedFile(fileUri)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { showTimeText(it.durationInSecods.toInt()) }
    }

    private fun initBackPress() {
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
    }

    private fun backPressConsumer(): Consumer<Timed<String>> {
        return Consumer {
            voizyFirebaseAnalytics.logRecordingCancel()
            navigateBackToLibrary()
        }
    }

    private fun initRecordEvents() {
        viewModel.getRecordingEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(recordingEventConsumer())
    }

    private fun recordingEventConsumer(): Consumer<AudioRecorder.RecordingEvent> {
        return Consumer {
            when (it) {
                AudioRecorder.RecordingEvent.STOP -> {
                    timerDisposable?.let {
                        it.dispose()
                    }
                    showSaveLayout()
                }
                AudioRecorder.RecordingEvent.START_FAILED -> {
                    Timber.e("Failed to start recording")
                }
                AudioRecorder.RecordingEvent.STOP_FAILED -> {
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
        val inMillis = (timeInSeconds).toLong() * 1000
        val dateFormatter = SimpleDateFormat("mm:ss")
        val timeString = dateFormatter.format(Date(inMillis))
        tv_recording_time.text = timeString.plus(" / 00:15")
    }

    private fun isFileSendAction(): Boolean {
        return arguments != null &&
            arguments!!.get(VoizyApp.KEY_ACTION) == Intent.ACTION_SEND
    }

    private fun showSaveLayout() {
        et_voizy_name.visibility = View.VISIBLE
        et_voizy_tags.visibility = View.VISIBLE
        btn_save_voizy.visibility = View.VISIBLE
        btn_playback.visibility = View.VISIBLE
    }

    private fun hideSoftKeyboard(view: View) {
        val inputMethodManager: InputMethodManager = context!!
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.rootView.windowToken, 0, null)
    }

    private fun getVoizyFromUserInputs(): Voizy {
        val locale = Locale.getDefault()
        return Voizy(
            name = et_voizy_name.text.toString(),
            tags = et_voizy_tags.getTags(),
            locale = locale.toString(),
            localeLang = locale.language,
            localeCountry = locale.country
        )
    }

    private fun navigateBackToLibrary() {
        if (isFileSendAction()) {

            val createOptionsFragment =
                fragmentManager!!.findFragmentById(R.id.record_button_fragment)

            fragmentManager!!.beginTransaction()
                .remove(this)
                .add(R.id.fragment_container, LibraryFragment(), null)
                .show(createOptionsFragment!!)
                .commit()
        } else {
            fragmentManager!!.popBackStackImmediate(
                TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }
}