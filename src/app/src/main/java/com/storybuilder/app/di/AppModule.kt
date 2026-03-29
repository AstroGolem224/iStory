package com.storybuilder.app.di

import android.content.Context
import com.storybuilder.app.data.storage.SecureApiKeyStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSecureApiKeyStorage(
        @ApplicationContext context: Context
    ): SecureApiKeyStorage {
        return SecureApiKeyStorage(context)
    }
}
