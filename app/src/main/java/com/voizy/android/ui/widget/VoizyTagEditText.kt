package com.voizy.android.ui.widget

import android.R
import android.content.Context
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.EditText
import timber.log.Timber

class VoizyTagEditText : EditText, TextWatcher {

    private val tagEditor = TagEditor()

    constructor(context: Context) : super(context)

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle)

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        setSelection(this.length())
    }

    init {
        this.addTextChangedListener(this)
    }

    override fun afterTextChanged(editable: Editable?) {
        if (editable.toString() != tagEditor.tagString) {
            val editedString = tagEditor.onTextChanged(editable.toString())
            Timber.d("afterTextChanged editedString $editedString")
            editable!!.replace(0, editable!!.length, editedString)
        } else {
            Timber.d("afterTextChanged same string")
        }

        val colorUntil = editable!!.lastIndexOf("#")
        if (colorUntil > 0) {
            val orangeColorSpan = ForegroundColorSpan(context!!.getColor(R.color.holo_orange_dark))
            editable.setSpan(orangeColorSpan, 0, colorUntil, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

            val blackColorSpan = ForegroundColorSpan(context!!.getColor(R.color.widget_edittext_dark))
            editable.setSpan(blackColorSpan, colorUntil, editable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    private class TagEditor {

        private var editedTagString: String = ""

        val tagString: String
            get() = editedTagString

        fun onTextChanged(inputString: String): String {

            if (inputString.isEmpty()) {
                editedTagString = inputString
                return editedTagString
            }

            Timber.d("onTextChanged() inputString")
            editedTagString = if (!inputString.startsWith("#")) {
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
}