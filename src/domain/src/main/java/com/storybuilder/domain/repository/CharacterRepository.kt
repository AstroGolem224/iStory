package com.storybuilder.domain.repository

import com.storybuilder.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getAllCharacters(): Flow<List<Character>>
    suspend fun getCharacterById(characterId: String): Character?
    suspend fun insertCharacter(character: Character)
    suspend fun deleteCharacter(characterId: String)
}
