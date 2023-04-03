package com.example.dogbreeds.data.repositories

import com.example.dogbreeds.BuildConfig
import com.example.dogbreeds.data.datasources.persistence.AppDatabase
import com.example.dogbreeds.data.datasources.remote.BreedDTO
import com.example.dogbreeds.data.datasources.remote.DogApiClient
import com.example.dogbreeds.data.datasources.remote.ImageDTO
import com.example.dogbreeds.toDomain
import com.example.dogbreeds.toLocal
import com.example.domain.Breed
import com.example.domain.repositories.BreedPage
import com.example.domain.repositories.IBreedsRepository
import io.ktor.client.call.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * TODO
 */
const val LIMIT = 20

/**
 * TODO
 */
const val REQUEST_DELAY = 1000L

/**
 * TODO
 */
val REQUEST_DELAY_ENABLED: Boolean = BuildConfig.REQUEST_DELAY_ENABLED

suspend fun enableRequestDelay() {
    if (REQUEST_DELAY_ENABLED) delay(REQUEST_DELAY)
}

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
                        hasNext = pageIndex * LIMIT < paginationCount,
                        breeds = List(LIMIT) { null },
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
                        hasNext = pageIndex * LIMIT < paginationCount,
                        breeds = localBreeds.map { it.toDomain() },
                    ))
                )

                return@flow
            }
        }

        try {
            // delay(3000)

            val response = withContext(Dispatchers.IO) {
                enableRequestDelay()

                api.getBreeds(LIMIT, pageIndex)
            }

            val breedDTOs: List<BreedDTO> = response.body()
            val total = response.headers["pagination-count"]?.toIntOrNull() ?: run {
                emit(Result.failure(IllegalStateException("Invalid pagination count")))

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
                        hasNext = pageIndex * LIMIT < total,
                        breeds = breedDTOs.map { it.toDomain() },
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Result.failure(exception))
        }
    }
}