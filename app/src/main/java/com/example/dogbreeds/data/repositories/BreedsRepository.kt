package com.example.dogbreeds.data.repositories

import android.util.Log
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
 * Breeds Repository
 */
class BreedsRepository(
    private val api: DogApiClient,
    private val database: AppDatabase,
    private val networkRepository: NetworkRepository,
) : IBreedsRepository {
    companion object {
        const val TAG = "BreedsRepository"
    }

    private var paginationCount = 0

    override suspend fun getBreedById(breedId: Int) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting breed with breedId=$breedId")

        runCatching {
            database.breedsDao().getBreedById(breedId).toDomain()
        }.getOrElse {
            Log.d(TAG, "Local fetch failed")

            enableRequestDelay()

            val breedDTO: BreedDTO = api.getBreedById(breedId).body()
            val imageDTO = breedDTO.reference_image_id?.let {
                api.getImageById(it).body<ImageDTO>()
            }
            breedDTO.toDomain().copy(imageUrl = imageDTO?.url)
        }
    }

    // TODO Could implement here a pagination as well but with a load more instead of next/prev keys
    override suspend fun searchBreedsByTerm(term: String): Result<List<Breed>> {
        Log.d(TAG, "Searching breeds with term=$term")

        return runCatching {
            if (networkRepository.networkAvailable.value) {
                Log.d(TAG, "No network available to fetch remotely")

                val response = withContext(Dispatchers.IO) {
                    enableRequestDelay()

                    api.searchBreeds(term)
                }

                val breedsDTO: List<BreedDTO> = response.body()

                Result.success(breedsDTO.map { it.toDomain() })
            } else {
                val breeds = withContext(Dispatchers.IO) {
                    database.breedsDao().searchBreedsByName(term)
                }

                Result.success(breeds.map { it.toDomain() })
            }
        }.getOrElse {
            Log.e(TAG, "Fetching failed", it)

            Result.failure(it)
        }
    }

    override suspend fun getBreedPage(pageIndex: Int, refresh: Boolean) = flow {
        Log.d(TAG, "Getting pages for pageIndex=$pageIndex and refresh=$refresh")

        // Emit placeholder data first
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

        // If there is no refresh requested just fetch data from local database
        if (!refresh) {
            Log.d(TAG, "No refresh, load from database first")

            val localBreeds = withContext(Dispatchers.IO) {
                database.breedsDao().getBreedsByPage(pageIndex)
            }

            if (localBreeds.isNotEmpty()) {
                Log.d(TAG, "Local fetch successful. breedsSize=${localBreeds.size}")

                paginationCount = localBreeds.first().total

                emit(
                    Result.success(
                        BreedPage(
                            hasPrev = pageIndex > 0,
                            hasNext = hasNextPage(pageIndex, PAGE_LIMIT, paginationCount),
                            breeds = localBreeds.map { it.toDomain() },
                            totalPages = calculateTotalPages(paginationCount, PAGE_LIMIT),
                        )
                    )
                )

                return@flow
            }
        }

        Log.d(TAG, "Refresh or local breeds returned empty")

        try {
            val response = withContext(Dispatchers.IO) {
                enableRequestDelay()

                api.getBreeds(PAGE_LIMIT, pageIndex)
            }

            val breedDTOs: List<BreedDTO> = response.body()
            val total = response.headers["pagination-count"]?.toIntOrNull() ?: run {
                Log.e(TAG, "Could not fetch pagination count from headers")

                emit(
                    Result.failure(IllegalStateException("Invalid pagination count"))
                )

                return@flow
            }
            paginationCount = total

            Log.d(TAG, "Save remote page locally")

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
            Log.e(TAG, "Error fetching page", exception)

            emit(Result.failure(exception))
        }
    }
}