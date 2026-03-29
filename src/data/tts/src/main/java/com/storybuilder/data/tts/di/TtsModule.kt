package com.storybuilder.data.tts.di

import android.content.Context
import com.storybuilder.data.tts.NarrationRepositoryImpl
import com.storybuilder.data.tts.TtsManager
import com.storybuilder.domain.repository.NarrationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TtsModule {

    @Binds
    @Singleton
    abstract fun bindNarrationRepository(
        impl: NarrationRepositoryImpl
    ): NarrationRepository

    companion object {
        @Provides
        @Singleton
        fun provideTtsManager(
            @ApplicationContext context: Context
        ): TtsManager {
            return TtsManager(context)
        }
    }
}
