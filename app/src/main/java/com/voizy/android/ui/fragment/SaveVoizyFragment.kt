package com.voizy.android.ui.fragment

import android.app.Activity
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
import com.voizy.android.audio.PlaybackEvent
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.widget.PlayPauseButton
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.utils.showTimeSecondsAndTenths
import com.voizy.android.viewmodels.SaveVoizyViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Timed
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.save_voizy_fragment.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.util.Locale
import java.util.concurrent.TimeUnit

class SaveVoizyFragment : BaseFragment() {

    private val viewModel: SaveVoizyViewModel by inject()
    private val backPressEvent = PublishSubject.create<String>()
    private val voizyFirebaseAnalytics: VoizyFirebaseAnalytics = get()

    companion object {
        val TAG = SaveVoizyFragment::class.java.simpleName!!
        private const val ACCEPT_BACK_THRESHOLD = 3000
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun useCustomBackPress(): Boolean {
        return false
    }

    override fun onBackPressed() {
        backPressEvent.onNext(TAG)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.save_voizy_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        initPlayback()
        initSave()
        initBackPress()
        initVoizyTime()
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
            voizyFirebaseAnalytics.logSaveVoizyCancel()
            fragmentManager!!.popBackStackImmediate(
                TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
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

    private fun initVoizyTime() {
        viewModel.getTempVoizyDurationInMillis()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { tv_recording_time.showTimeSecondsAndTenths(it) }
    }
}