package com.storybuilder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val toneGuidelines: String,
    val themeColor: String,
    val iconAsset: String,
    val isDefault: Boolean = false
)
