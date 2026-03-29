package com.storybuilder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.storybuilder.data.local.converter.Converters
import com.storybuilder.data.local.dao.CharacterDao
import com.storybuilder.data.local.dao.ChatMessageDao
import com.storybuilder.data.local.dao.GenreDao
import com.storybuilder.data.local.dao.StoryBeatDao
import com.storybuilder.data.local.dao.StoryDao
import com.storybuilder.data.local.entity.CharacterEntity
import com.storybuilder.data.local.entity.ChatMessageEntity
import com.storybuilder.data.local.entity.GenreEntity
import com.storybuilder.data.local.entity.StoryBeatEntity
import com.storybuilder.data.local.entity.StoryEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        StoryEntity::class,
        StoryBeatEntity::class,
        GenreEntity::class,
        CharacterEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun storyDao(): StoryDao
    abstract fun storyBeatDao(): StoryBeatDao
    abstract fun genreDao(): GenreDao
    abstract fun characterDao(): CharacterDao
}
