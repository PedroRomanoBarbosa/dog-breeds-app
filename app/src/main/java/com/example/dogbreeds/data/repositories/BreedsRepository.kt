package com.example.dogbreeds.data.repositories

import com.example.dogbreeds.*
import com.example.dogbreeds.Configuration.PAGE_LIMIT
import com.example.dogbreeds.data.datasources.persistence.AppDatabase
import com.example.dogbreeds.data.datasources.remote.BreedDTO
import com.example.dogbreeds.data.datasources.remote.DogApiClient
import com.example.dogbreeds.data.datasources.remote.ImageDTO
import com.example.domain.Breed
import com.example.domain.repositories.BreedPage
import com.example.domain.repositories.IBreedsRepository
import io.ktor.client.call.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * TODO
 */
class BreedsRepository(
    private val api: DogApiClient,
    private val database: AppDatabase,
) : IBreedsRepository {
    companion object {
        const val TAG = "BreedsRepository"
    }

    private var paginationCount = 0

    override suspend fun getBreedById(breedId: Int) = withContext(Dispatchers.IO) {
        runCatching {
            database.breedsDao().getBreedById(breedId).toDomain()
        }.getOrElse {
            enableRequestDelay()

            val breedDTO: BreedDTO = api.getBreedById(breedId).body()
            val imageDTO = breedDTO.reference_image_id?.let {
                api.getImageById(it).body<ImageDTO>()
            }
            breedDTO.toDomain().copy(imageUrl = imageDTO?.url)
        }
    }

    override suspend fun searchBreedsByTerm(term: String): List<Breed> {
        // TODO

        val response = withContext(Dispatchers.IO) {
            enableRequestDelay()

            api.searchBreeds(term)
        }

        val breedsDTO: List<BreedDTO> = response.body()

        return breedsDTO.map { it.toDomain() }
    }

    override suspend fun getBreedPage(pageIndex: Int, refresh: Boolean) = flow {
        if (!refresh) {
            emit(
                Result.success(
                    BreedPage(
                        hasPrev = pageIndex > 0,
                        hasNext = hasNextPage(pageIndex, PAGE_LIMIT, paginationCount),
                        breeds = List(PAGE_LIMIT) { null },
                        totalPages = calculateTotalPages(paginationCount, PAGE_LIMIT),
                    )
                )
            )

            val localBreeds = withContext(Dispatchers.IO) {
                database.breedsDao().getBreedsByPage(pageIndex)
            }

            if (localBreeds.isNotEmpty()) {
                paginationCount = localBreeds.first().total

                emit(
                    Result.success(
                    BreedPage(
                        hasPrev = pageIndex > 0,
                        hasNext = hasNextPage(pageIndex, PAGE_LIMIT, paginationCount),
                        breeds = localBreeds.map { it.toDomain() },
                        totalPages = calculateTotalPages(paginationCount, PAGE_LIMIT),
                    ))
                )

                return@flow
            }
        }

        try {
            val response = withContext(Dispatchers.IO) {
                enableRequestDelay()

                api.getBreeds(PAGE_LIMIT, pageIndex)
            }

            val breedDTOs: List<BreedDTO> = response.body()
            val total = response.headers["pagination-count"]?.toIntOrNull() ?: run {
                emit(
                    Result.failure(IllegalStateException("Invalid pagination count"))
                )

                return@flow
            }
            paginationCount = total

            withContext(Dispatchers.IO) {
                database.breedsDao().insertAll(
                    breeds = breedDTOs.map {
                        it.toLocal(page = pageIndex, total = paginationCount)
                    },
                )
            }

            emit(
                Result.success(
                    BreedPage(
                        hasPrev = pageIndex > 0,
                        hasNext = hasNextPage(pageIndex, PAGE_LIMIT, paginationCount),
                        breeds = breedDTOs.map { it.toDomain() },
                        totalPages = calculateTotalPages(paginationCount, PAGE_LIMIT),
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Result.failure(exception))
        }
    }
}