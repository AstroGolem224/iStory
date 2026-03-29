package com.storybuilder.domain.repository

import com.storybuilder.domain.model.Genre
import kotlinx.coroutines.flow.Flow

interface GenreRepository {
    fun getAllGenres(): Flow<List<Genre>>
    suspend fun getGenreById(genreId: String): Genre?
    suspend fun insertGenre(genre: Genre)
    suspend fun insertAllGenres(genres: List<Genre>)
    suspend fun getGenreCount(): Int
}
