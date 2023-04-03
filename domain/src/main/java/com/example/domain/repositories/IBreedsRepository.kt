package com.example.domain.repositories

import com.example.domain.Breed
import kotlinx.coroutines.flow.Flow

/**
 * Represents a page of dog [Breed]s. Includes information about next, previous and total pages
 */
data class BreedPage(
    val breeds: List<Breed?>,
    val hasPrev: Boolean,
    val hasNext: Boolean,
    val totalPages: Int,
)

/**
 * Domain interface that describes a Breed Repository
 */
interface IBreedsRepository {
    suspend fun getBreedPage(pageIndex: Int, refresh: Boolean = false): Flow<Result<BreedPage>>

    suspend fun getBreedById(breedId: Int): Breed

    suspend fun searchBreedsByTerm(term: String): Result<List<Breed>>
}