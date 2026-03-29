package com.storybuilder.data.local.repository

import com.storybuilder.data.local.dao.GenreDao
import com.storybuilder.data.local.entity.GenreEntity
import com.storybuilder.domain.model.Genre
import com.storybuilder.domain.repository.GenreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GenreRepositoryImpl @Inject constructor(
    private val genreDao: GenreDao
) : GenreRepository {

    override fun getAllGenres(): Flow<List<Genre>> {
        return genreDao.getAllGenres().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGenreById(genreId: String): Genre? {
        return genreDao.getGenreById(genreId)?.toDomain()
    }

    override suspend fun insertGenre(genre: Genre) {
        genreDao.insertGenre(genre.toEntity())
    }

    override suspend fun insertAllGenres(genres: List<Genre>) {
        genreDao.insertAllGenres(genres.map { it.toEntity() })
    }

    override suspend fun getGenreCount(): Int {
        return genreDao.getGenreCount()
    }

    private fun GenreEntity.toDomain(): Genre {
        return Genre(
            id = id,
            name = name,
            description = description,
            toneGuidelines = toneGuidelines,
            themeColor = themeColor,
            iconAsset = iconAsset
        )
    }

    private fun Genre.toEntity(): GenreEntity {
        return GenreEntity(
            id = id,
            name = name,
            description = description,
            toneGuidelines = toneGuidelines,
            themeColor = themeColor,
            iconAsset = iconAsset
        )
    }
}
