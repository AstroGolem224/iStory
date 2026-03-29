package com.storybuilder.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor() {
    // Stub for Firebase client - will be implemented in future phase
    suspend fun syncStory(storyId: String): Result<Unit> {
        return Result.success(Unit)
    }

    suspend fun backupData(): Result<Unit> {
        return Result.success(Unit)
    }
}
