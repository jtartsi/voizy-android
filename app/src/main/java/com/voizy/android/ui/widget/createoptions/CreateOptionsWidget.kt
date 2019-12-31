package com.voizy.android.ui.widget.createoptions

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding2.view.RxView
import com.voizy.android.R
import io.reactivex.Observable
import kotlinx.android.synthetic.main.create_options_layout.view.*

class CreateOptionsWidget : ConstraintLayout {

    enum class State { OPEN, CLOSED, RECORDING }

    var state: State = State.CLOSED
        set(value) {
            handleButtonStateChange(field, value)
            field = value
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(
        context, attributeSet, 0
    )

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttributeSet: Int) : super(
        context,
        attributeSet,
        defStyleAttributeSet
    ) {
        inflate(context, R.layout.create_options_layout, this)
        initOpenClose()
    }

    fun getButtonEvents(): Observable<CreateEvent> {
        return fileImportEvents()
            .mergeWith(cloudPullEvents())
            .mergeWith(btn_rec_mic.getButtonEvents())
    }

    private fun fileImportEvents(): Observable<CreateEvent> {
        return RxView.clicks(btn_choose_file)
            .map { CreateEvent.CHOOSE_FILE }
    }

    private fun cloudPullEvents(): Observable<CreateEvent> {
        return RxView.clicks(btn_choose_cloud)
            .map { CreateEvent.CHOOSE_CLOUD }
    }

    private fun initOpenClose() {
        btn_open_options.setOnClickListener { state = State.OPEN }
        btn_close_options.setOnClickListener { state = State.CLOSED }
    }

    private fun handleButtonStateChange(previousState: State, newState: State) {
        if (previousState == State.RECORDING && newState != State.RECORDING) {
            btn_rec_mic.handleStopRecording()
        }

        if (newState == State.OPEN) {
            showOptions()
        } else if (newState == State.CLOSED) {
            hideOptions()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showOptions() {
        btn_open_options.visibility = View.INVISIBLE
        btn_choose_cloud.visibility = View.VISIBLE
        btn_choose_file.visibility = View.VISIBLE
        btn_close_options.visibility = View.VISIBLE
    }

    @SuppressLint("RestrictedApi")
    private fun hideOptions() {
        btn_open_options.visibility = View.VISIBLE
        btn_choose_cloud.visibility = View.INVISIBLE
        btn_choose_file.visibility = View.INVISIBLE
        btn_close_options.visibility = View.INVISIBLE
    }
}