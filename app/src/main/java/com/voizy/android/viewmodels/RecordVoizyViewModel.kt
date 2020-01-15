package com.voizy.android.viewmodels

import com.voizy.android.audio.AudioRecorder

class RecordVoizyViewModel(
    private val voizyRecorder: AudioRecorder
) : DisposingViewModel()