package com.storybuilder.data.local.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.storybuilder.data.local.database.AppDatabase
import com.storybuilder.data.local.entity.GenreEntity
import com.storybuilder.data.local.repository.ChatMessageRepositoryImpl
import com.storybuilder.data.local.repository.CharacterRepositoryImpl
import com.storybuilder.data.local.repository.GenreRepositoryImpl
import com.storybuilder.data.local.repository.ProviderConfigRepositoryImpl
import com.storybuilder.data.local.repository.StoryBeatRepositoryImpl
import com.storybuilder.data.local.repository.StoryRepositoryImpl
import com.storybuilder.domain.repository.CharacterRepository
import com.storybuilder.domain.repository.ChatMessageRepository
import com.storybuilder.domain.repository.GenreRepository
import com.storybuilder.domain.repository.ProviderConfigRepository
import com.storybuilder.domain.repository.StoryBeatRepository
import com.storybuilder.domain.repository.StoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataModule {

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "story_builder_database"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate genres on database creation
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = provideDatabase(context)
                            prepopulateGenres(database)
                        }
                    }
                })
                .build()
        }

        private suspend fun prepopulateGenres(database: AppDatabase) {
            val genreDao = database.genreDao()
            if (genreDao.getGenreCount() == 0) {
                val defaultGenres = listOf(
                    GenreEntity(
                        id = "horror",
                        name = "Horror",
                        description = "Cosmic dread, psychological terror",
                        toneGuidelines = "Create an atmosphere of dread and unease. Use sensory details that evoke fear. " +
                            "Emphasize the unknown, isolation, and the fragility of human existence.",
                        themeColor = "#8B0000",
                        iconAsset = "ic_horror",
                        isDefault = true
                    ),
                    GenreEntity(
                        id = "fantasy",
                        name = "Fantasy",
                        description = "High magic, epic quests",
                        toneGuidelines = "Emphasize wonder, magic, and heroism. Create rich, immersive world-building. " +
                            "Include elements of the extraordinary and mythical.",
                        themeColor = "#4B0082",
                        iconAsset = "ic_fantasy",
                        isDefault = true
                    ),
                    GenreEntity(
                        id = "scifi",
                        name = "Sci-Fi",
                        description = "Future tech, space exploration",
                        toneGuidelines = "Focus on technological advancement, space exploration, and futuristic societies. " +
                            "Explore themes of humanity's place in the universe.",
                        themeColor = "#0066CC",
                        iconAsset = "ic_scifi",
                        isDefault = true
                    ),
                    GenreEntity(
                        id = "thriller",
                        name = "Thriller",
                        description = "Crime, mystery, suspense",
                        toneGuidelines = "Build tension and suspense. Keep stakes high and time pressure real. " +
                            "Use twists and revelations.",
                        themeColor = "#2F4F4F",
                        iconAsset = "ic_thriller",
                        isDefault = true
                    ),
                    GenreEntity(
                        id = "adventure",
                        name = "Adventure",
                        description = "Exploration, survival",
                        toneGuidelines = "Emphasize action, discovery, and overcoming challenges. Create vivid exotic locations. " +
                            "Focus on resourcefulness and courage.",
                        themeColor = "#228B22",
                        iconAsset = "ic_adventure",
                        isDefault = true
                    ),
                    GenreEntity(
                        id = "romance",
                        name = "Romance",
                        description = "Relationships, emotional journeys",
                        toneGuidelines = "Focus on emotional depth and character relationships. Build meaningful connections. " +
                            "Explore themes of love, trust, and personal growth.",
                        themeColor = "#FF69B4",
                        iconAsset = "ic_romance",
                        isDefault = true
                    )
                )
                genreDao.insertAllGenres(defaultGenres)
            }
        }

        @Provides
        fun provideChatMessageDao(database: AppDatabase) = database.chatMessageDao()

        @Provides
        fun provideStoryDao(database: AppDatabase) = database.storyDao()

        @Provides
        fun provideStoryBeatDao(database: AppDatabase) = database.storyBeatDao()

        @Provides
        fun provideGenreDao(database: AppDatabase) = database.genreDao()

        @Provides
        fun provideCharacterDao(database: AppDatabase) = database.characterDao()
    }

    @Binds
    abstract fun bindChatMessageRepository(
        impl: ChatMessageRepositoryImpl
    ): ChatMessageRepository

    @Binds
    abstract fun bindStoryRepository(
        impl: StoryRepositoryImpl
    ): StoryRepository

    @Binds
    abstract fun bindStoryBeatRepository(
        impl: StoryBeatRepositoryImpl
    ): StoryBeatRepository

    @Binds
    abstract fun bindGenreRepository(
        impl: GenreRepositoryImpl
    ): GenreRepository

    @Binds
    abstract fun bindCharacterRepository(
        impl: CharacterRepositoryImpl
    ): CharacterRepository

    @Binds
    abstract fun bindProviderConfigRepository(
        impl: ProviderConfigRepositoryImpl
    ): ProviderConfigRepository
}
