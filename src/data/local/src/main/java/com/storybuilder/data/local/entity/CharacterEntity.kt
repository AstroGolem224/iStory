package com.storybuilder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val archetype: String,
    val traits: String, // JSON list of traits
    val backstory: String?,
    val createdAt: Long
)
