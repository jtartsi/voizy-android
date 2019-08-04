package com.voizy.android.middleware.repositories

class VoizyRepository(
    private val voizyFirestore: VoizyFirestore,
    private val localFileManager: LocalFileManager
)