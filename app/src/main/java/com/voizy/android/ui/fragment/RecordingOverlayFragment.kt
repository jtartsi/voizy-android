package com.voizy.android.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.ui.model.Voizy
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.utils.showProgressBar
import com.voizy.android.viewmodels.RecordingOverlayViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.recording_overlay_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RecordingOverlayFragment : Fragment(), TextWatcher {

    private val viewModel: RecordingOverlayViewModel by inject<RecordingOverlayViewModel>()
    private lateinit var playButton: View
    private var timer: Disposable? = null
    private val tagEditor = TagEditor()

    private class TagEditor() {

        private var nonModifiedTagString: String = ""
        private var editedTagString: String = ""

        val tagString: String
            get() = editedTagString

        // fun onTextChanged(inputString: String): String {
        //     nonModifiedTagString = inputString
        //
        //     if (nonModifiedTagString.isEmpty()) {
        //         return nonModifiedTagString
        //     }
        //
        //     if (nonModifiedTagString.endsWith("  ")) {
        //         editedTagString.removeRange(editedTagString.length, editedTagString.length)
        //         return editedTagString
        //     }
        //
        //     val tags = nonModifiedTagString.split(" ")
        //     editedTagString = ""
        //     for (tag in tags) {
        //
        //         if (tags.last().equals(tag)) {
        //             editedTagString = editedTagString.plus(tag)
        //             break
        //         }
        //
        //         Timber.d("onTextChanged tag $tag")
        //         editedTagString = if (tag.contains("#")) {
        //             Timber.d("onTextChanged tag has hashtag")
        //             editedTagString.plus(tag).plus(" ")
        //         } else {
        //             Timber.d("onTextChanged editing tag $tag")
        //             val editedTag = StringBuilder(tag).insert(0, "#").toString()
        //             Timber.d("onTextChanged edited tag $editedTag")
        //
        //             editedTagString.plus(editedTag).plus(" ")
        //         }
        //         Timber.d("onTextChanged editedTagString $editedTagString")
        //     }
        //     return editedTagString
        // }

        fun onTextChanged(inputString: String): String {
            Timber.d("onTextChanged() inputString")
            editedTagString = if (inputString.isNotEmpty() && !inputString.startsWith("#")) {
                "#".plus(inputString)
            } else {
                inputString
            }
            editedTagString = editedTagString
                .replace(" ", "#")
                .replace("##", "#")

            Timber.d("onTextChanged() outputString $editedTagString")
            return editedTagString
        }
    }

    override fun afterTextChanged(editable: Editable?) {
        Timber.d("afterTextChanged $editable")
        // Timber.d("afterTextChanged last ${editable!!.last()}")

        if (editable.toString() != tagEditor.tagString) {
            val editedString = tagEditor.onTextChanged(editable.toString())
            Timber.d("afterTextChanged editedString $editedString")
            editable!!.replace(0, editable!!.length, editedString)
        } else {
            Timber.d("afterTextChanged same string")
        }

        val colorUntil = editable!!.lastIndexOf("#")
        if (colorUntil > 0) {
            val orangeColorSpan = ForegroundColorSpan(context!!.getColor(android.R.color.holo_orange_dark))
            editable.setSpan(orangeColorSpan, 0, colorUntil, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

            val blackColorSpan = ForegroundColorSpan(context!!.getColor(android.R.color.widget_edittext_dark))
            editable.setSpan(blackColorSpan, colorUntil, editable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Timber.d("beforeTextChanged $s")
        // if (s!!.last()) {
        //     Timber.d("BEFORE User entered space")
        // }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Timber.d("onTextChanged $s")
        // if (s!!.isNotEmpty() && s!!.last().equals(" ")) {
        //     Timber.d("onTextChanged user inputted space")
        // }
    }

    companion object {
        public val TAG = RecordingOverlayFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recording_overlay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playButton = view.findViewById<View>(R.id.btn_play_preview)
        playButton.setOnClickListener {
            viewModel.playVoizy()
        }

        btn_save_voizy.setOnClickListener {
            val tags = et_voizy_tags.text.toString().split(" ").toList()
            val voizyToSave = Voizy(et_voizy_name.text.toString(), tags)
            viewModel.saveVoizy(voizyToSave)
            activity!!.showProgressBar(true)
            close()
        }

        et_voizy_tags.addTextChangedListener(this)
    }

    override fun onStart() {
        super.onStart()
        viewModel.recordingEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                when (it) {
                    VoizyRecorder.RecordingEvents.STARTED -> {
                        startTimer()
                        playButton.visibility = View.GONE
                        et_voizy_name.visibility = View.GONE
                        et_voizy_tags.visibility = View.GONE
                        btn_save_voizy.visibility = View.GONE
                    }
                    VoizyRecorder.RecordingEvents.FINISHED -> {
                        stopTimer()
                        playButton.visibility = View.VISIBLE
                        et_voizy_name.visibility = View.VISIBLE
                        et_voizy_tags.visibility = View.VISIBLE
                        btn_save_voizy.visibility = View.VISIBLE
                    }
                    VoizyRecorder.RecordingEvents.START_FAILED -> {
                        Timber.e("Failed to start recording")
                    }
                    VoizyRecorder.RecordingEvents.CLOSE_FAILED -> {
                        Timber.e("Failed to close recording")
                    }
                }
            }
    }

    private fun close() {
        hideSoftKeyboard(playButton)
        fragmentManager!!.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun startTimer() {
        tv_recording_time.text = "00:00 / 00:15"
        timer = Observable.intervalRange(
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

    private fun stopTimer() {
        timer?.let { it.dispose() }
    }

    private fun hideSoftKeyboard(view: View) {
        val inputMethodManager: InputMethodManager = context!!
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.rootView.windowToken, 0, null)
    }

    private fun parseTags() {
    }
}