package com.example.domain.repositories

import com.example.domain.Breed
import kotlinx.coroutines.flow.Flow

/**
 * TODO
 */
data class BreedPage(
    val breeds: List<Breed?>,
    val hasPrev: Boolean,
    val hasNext: Boolean,
)

/**
 * TODO
 */
interface IBreedsRepository {
    suspend fun getBreedPage(pageIndex: Int, refresh: Boolean = false): Flow<Result<BreedPage>>

    suspend fun getBreedById(breedId: Int): Breed

    suspend fun searchBreedsByTerm(term: String): List<Breed>
}