package com.example.dogbreeds.data.repositories

import com.example.dogbreeds.data.datasources.persistence.AppDatabase
import com.example.dogbreeds.data.datasources.persistence.BreedLocal
import com.example.dogbreeds.data.datasources.remote.BreedDTO
import com.example.dogbreeds.data.datasources.remote.DogApiClient
import com.example.domain.Breed
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
interface IBreedsRepository {
    suspend fun getBreedPage(pageIndex: Int, refresh: Boolean = false): Flow<Result<BreedPage>>
}

data class BreedPage(
    val breeds: List<Breed?>,
    val hasPrev: Boolean,
    val hasNext: Boolean,
)

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
                        breeds = localBreeds.map { Breed(id = it.id, name = it.name, imageUrl = it.imageUrl) },
                    ))
                )

                return@flow
            }
        }

        try {
            delay(3000)

            val response = withContext(Dispatchers.IO) {
                api.getBreeds(LIMIT, pageIndex)
            }

            val breeds: List<BreedDTO> = response.body()
            val total = response.headers["pagination-count"]?.toIntOrNull() ?: 0
            paginationCount = total

            withContext(Dispatchers.IO) {
                database.breedsDao().insertAll(breeds = breeds.map {
                    BreedLocal(
                        id = it.id,
                        name = it.name,
                        imageUrl = it.image.url,
                        page = pageIndex,
                        total = paginationCount,
                    )
                })
            }

            emit(
                Result.success(
                    BreedPage(
                        hasPrev = pageIndex > 0,
                        hasNext = pageIndex * LIMIT < total,
                        breeds = breeds.map { Breed(id = it.id, name = it.name, it.image.url) },
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Result.failure(exception))
        }
    }
}