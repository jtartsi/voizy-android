package com.voizy.android.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
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
import java.text.SimpleDateFormat
import java.util.Date

class AudioClipFragment : BaseFragment() {

    private val viewModel: AudioClipViewModel by inject()

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
                val fileUri = arguments!!.get(VoizyApp.KEY_DATA) as Uri
                viewModel.saveReceivedFile(fileUri)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                initDurationSeek(it)
                initStartPosSeek(it)
                initMoveStartPosForward()
                initMoveStartPosBackward()
                initPlayback()
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

    private fun initDurationSeek(importedData: ImportedData) {
        val maxInMillis = if (importedData.durationInMillis < MAX_DURATION_MS)
            importedData.durationInMillis else MAX_DURATION_MS

        sb_audio_clip_duration.max = maxInMillis.toInt()
        sb_audio_clip_duration.progress = maxInMillis.toInt()

        updateAudioDurationText(maxInMillis)

        sb_audio_clip_duration.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                selectedDuration: Int,
                fromUser: Boolean
            ) {
                updateAudioDurationText(selectedDuration.toLong())

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
        tv_audio_clip_position.text = getString(R.string.audio_start_pos, formatAudio(0))

        sb_audio_clip_position.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                startPos: Int,
                fromUser: Boolean
            ) {

                updateStartPositionText(startPos.toLong())

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
    }

    private fun initMoveStartPosForward() {
        btn_audio_clip_ffwd.setOnClickListener {
            sb_audio_clip_position.progress = sb_audio_clip_position.progress + MOVE_ON_BUTTON_MS
            updateDurationPosition(sb_audio_clip_duration.progress)
        }
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
            val newDuration =
                sb_audio_clip_position.max - startPos

            sb_audio_clip_duration.progress = newDuration
        }
    }

    private fun updateStartPosSecondaryProgress() {
        sb_audio_clip_position.secondaryProgress =
            sb_audio_clip_position.progress +
                sb_audio_clip_duration.progress
    }

    private fun updateAudioDurationText(timeInMs: Long) {
        tv_audio_clip_duration.text =
            getString(R.string.audio_duration, formatAudio(timeInMs))
    }

    private fun updateStartPositionText(timeInMs: Long) {
        tv_audio_clip_position.text =
            getString(R.string.audio_start_pos, formatAudio(timeInMs))
    }

    private fun formatAudio(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("s.S")
        return dateFormat.format(Date(timeInMillis).time)
    }
}