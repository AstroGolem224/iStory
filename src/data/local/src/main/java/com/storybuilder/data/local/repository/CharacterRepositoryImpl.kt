package com.storybuilder.data.local.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.storybuilder.data.local.dao.CharacterDao
import com.storybuilder.data.local.entity.CharacterEntity
import com.storybuilder.domain.model.Character
import com.storybuilder.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao
) : CharacterRepository {

    private val gson = Gson()

    override fun getAllCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCharacterById(characterId: String): Character? {
        return characterDao.getCharacterById(characterId)?.toDomain()
    }

    override suspend fun insertCharacter(character: Character) {
        characterDao.insertCharacter(character.toEntity())
    }

    override suspend fun deleteCharacter(characterId: String) {
        characterDao.deleteCharacter(characterId)
    }

    private fun CharacterEntity.toDomain(): Character {
        val traitsList: List<String> = try {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(traits, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return Character(
            id = id,
            name = name,
            archetype = archetype,
            traits = traitsList,
            backstory = backstory,
            createdAt = createdAt
        )
    }

    private fun Character.toEntity(): CharacterEntity {
        return CharacterEntity(
            id = id,
            name = name,
            archetype = archetype,
            traits = gson.toJson(traits),
            backstory = backstory,
            createdAt = createdAt
        )
    }
}
