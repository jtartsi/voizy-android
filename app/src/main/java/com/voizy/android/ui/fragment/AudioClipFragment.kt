package com.voizy.android.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.jakewharton.rxbinding2.view.RxView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.audio.PlaybackEvent
import com.voizy.android.ui.model.ImportedData
import com.voizy.android.ui.widget.PlayPauseButton
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.AudioClipViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.audio_clip_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.SimpleDateFormat

class AudioClipFragment : BaseFragment() {

    private val viewModel: AudioClipViewModel by inject()
    private val longPressWindHandler = Handler()

    companion object {
        public val TAG = AudioClipFragment::class.java.simpleName
        const val MAX_DURATION_MS = 15000L
        const val MOVE_ON_BUTTON_MS = 50
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.audio_clip_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        initFileImport()
        initPlayback()
        initClipAudio()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopPlayback()
            .autoDisposable(getScopeProvider())
            .subscribe()
    }

    private fun initFileImport() {
        Observable
            .defer {
                val filePath = arguments!!.getString(VoizyApp.KEY_DATA)
                viewModel.getImportedData(filePath)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                initDurationSeek(it)
                initStartPosSeek(it)
                initMoveStartPosForward()
                initMoveStartPosBackward()
            }
    }

    private fun initPlayback() {
        RxView.clicks(btn_audio_clip_play)
            .map {
                Pair(
                    sb_audio_clip_position.progress,
                    sb_audio_clip_position.progress + sb_audio_clip_duration.progress
                )
            }
            .flatMap { viewModel.togglePlay(it.first, it.second) }
            .autoDisposable(getScopeProvider())
            .subscribe()

        viewModel.playbackEvents
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (it.playbackEvent == PlaybackEvent.START) {
                    btn_audio_clip_play.state = PlayPauseButton.State.STOP_ICON
                } else if (it.playbackEvent == PlaybackEvent.STOP) {
                    btn_audio_clip_play.state = PlayPauseButton.State.PLAY_ICON
                }
            }
    }

    private fun initClipAudio() {
        RxView.clicks(btn_audio_clip_next)
            .flatMap {
                val startPosInMs = sb_audio_clip_position.progress.toLong()
                val endPosInMs = startPosInMs + sb_audio_clip_duration.progress.toLong()
                viewModel.clipAudio(startPosInMs, endPosInMs)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe({
                fragmentManager!!.beginTransaction()
                    .replace(R.id.fragment_container, SaveVoizyFragment(), SaveVoizyFragment.TAG)
                    .addToBackStack(SaveVoizyFragment.TAG)
                    .commit()
            }, {
                Timber.e(it, "initClipAudio onError()")
            })
    }

    private fun initDurationSeek(importedData: ImportedData) {
        val maxInMillis = if (importedData.durationInMillis < MAX_DURATION_MS)
            importedData.durationInMillis else MAX_DURATION_MS

        sb_audio_clip_duration.max = maxInMillis.toInt()
        sb_audio_clip_duration.progress = maxInMillis.toInt()

        tv_audio_clip_duration_value.text = formatAudioTime(maxInMillis)

        sb_audio_clip_duration.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                selectedDuration: Int,
                fromUser: Boolean
            ) {
                tv_audio_clip_duration_value.text = formatAudioTime(selectedDuration.toLong())

                if (fromUser) {
                    updateDurationPosition(selectedDuration)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun initStartPosSeek(importedData: ImportedData) {
        val maxInMillis = importedData.durationInMillis

        sb_audio_clip_position.max = maxInMillis.toInt()
        tv_audio_clip_position_value.text =
            formatAudioTime(sb_audio_clip_position.progress.toLong())

        sb_audio_clip_position.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                startPos: Int,
                fromUser: Boolean
            ) {
                tv_audio_clip_position_value.text = formatAudioTime(startPos.toLong())

                if (fromUser) {
                    updateStartPosition(startPos)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        updateStartPosSecondaryProgress()
    }

    private fun initMoveStartPosBackward() {
        btn_audio_clip_rwd.setOnClickListener {
            sb_audio_clip_position.progress = sb_audio_clip_position.progress - MOVE_ON_BUTTON_MS
            updateDurationPosition(sb_audio_clip_duration.progress)
        }

        btn_audio_clip_rwd.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Timber.d("winding action_down")
                    longPressWind(-50)
                }
                MotionEvent.ACTION_UP -> {
                    Timber.d("winding action_up")
                    longPressWindHandler.removeCallbacksAndMessages(null)
                }
            }
            false
        }
    }

    private fun initMoveStartPosForward() {
        btn_audio_clip_ffwd.setOnClickListener {
            sb_audio_clip_position.progress = sb_audio_clip_position.progress + MOVE_ON_BUTTON_MS
            updateDurationPosition(sb_audio_clip_duration.progress)
        }

        btn_audio_clip_ffwd.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Timber.d("winding action_down")
                    longPressWind(50)
                }
                MotionEvent.ACTION_UP -> {
                    Timber.d("winding action_up")
                    longPressWindHandler.removeCallbacksAndMessages(null)
                }
            }
            false
        }
    }

    private fun longPressWind(delta: Int) {
        Timber.d("winding longPressWind delta $delta")
        longPressWindHandler.postDelayed({
            Timber.d("winding longPressWind postDelayed delta $delta")
            sb_audio_clip_position.progress = sb_audio_clip_position.progress + delta
            longPressWind((delta * 1.1).toInt())
        }, 200)
    }

    private fun updateDurationPosition(durationPos: Int) {
        updateStartPosSecondaryProgress()

        if (durationPos + sb_audio_clip_position.progress
            > sb_audio_clip_position.max
        ) {
            val newStartPos =
                sb_audio_clip_position.max - durationPos

            sb_audio_clip_position.progress = newStartPos
        }
    }

    private fun updateStartPosition(startPos: Int) {
        updateStartPosSecondaryProgress()

        if (startPos + sb_audio_clip_duration.progress > sb_audio_clip_position.max) {
            sb_audio_clip_position.progress =
                sb_audio_clip_position.max - sb_audio_clip_duration.progress
        }
    }

    private fun updateStartPosSecondaryProgress() {
        sb_audio_clip_position.secondaryProgress =
            sb_audio_clip_position.progress +
                sb_audio_clip_duration.progress
    }

    private fun formatAudioTime(timeInMillis: Long): String {
        val stringBuilder = StringBuilder()
        return if (timeInMillis > 60000) {
            val timeFormat = SimpleDateFormat("mm:ss.S")
            stringBuilder.append(timeFormat.format(timeInMillis)).toString()
        } else {
            val timeFormat = SimpleDateFormat("s.S")
            stringBuilder.append(timeFormat.format(timeInMillis)).append("s").toString()
        }
    }
}