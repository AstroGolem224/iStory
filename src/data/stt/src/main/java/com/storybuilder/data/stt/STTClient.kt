package com.storybuilder.data.stt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class STTClient @Inject constructor() {
    // Stub for Speech-to-Text client - will be implemented in future phase
    fun startListening(): Flow<String> = flow {
        emit("")
    }

    fun stopListening() {
        // Stub implementation
    }
}
