package com.example.dogbreeds.data.datasources

import androidx.paging.*
import androidx.room.withTransaction
import com.example.dogbreeds.data.datasources.persistence.AppDatabase
import com.example.dogbreeds.data.datasources.persistence.BreedLocal
import com.example.dogbreeds.data.datasources.persistence.BreedRemoteKeys
import com.example.dogbreeds.data.datasources.remote.DogApiClient
import com.example.dogbreeds.data.datasources.remote.BreedDTO
import com.example.dogbreeds.toLocal
import io.ktor.client.call.*

/**
 * TODO
 */
@OptIn(ExperimentalPagingApi::class)
class BreedsRemoteMediator(
    private val initialPage: Int = 0,
    private val database: AppDatabase,
    private val apiClient: DogApiClient,
) : RemoteMediator<Int, BreedLocal>() {
    private val breedsDao = database.breedsDao()
    private val remoteKeysDao = database.remoteKeysDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, BreedLocal>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = state.anchorPosition?.let { position ->
                    state.closestItemToPosition(position)?.id?.let { id ->
                        database.withTransaction {
                            remoteKeysDao.remoteKeysByBreedId(id.toLong())
                        }
                    }
                }

                remoteKeys?.nextKey?.minus(1) ?: initialPage
            }

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                val remoteKeys = state.lastItemOrNull()?.let { breed ->
                    database.withTransaction {
                        remoteKeysDao.remoteKeysByBreedId(breed.id.toLong())
                    }
                }

                remoteKeys?.nextKey ?: return MediatorResult.Success(true)
            }
        }

        val response = runCatching {
            apiClient.getBreeds(limit = state.config.pageSize, page)
        }.getOrElse {
            return MediatorResult.Error(it)
        }
        val breeds: List<BreedDTO> = response.body()

        val endOfPaginationReached = breeds.size < state.config.pageSize

        database.withTransaction {
            if (loadType == LoadType.REFRESH) {
                remoteKeysDao.clearRemoteKeys()
                breedsDao.clearAll()
            }
            val prevKey = if (page == initialPage) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1
            val keys = breeds.map {
                BreedRemoteKeys(
                    breedId = it.id.toLong(),
                    prevKey = prevKey,
                    nextKey = nextKey,
                )
            }
            remoteKeysDao.insertAll(keys)
            breedsDao.insertAll(breeds.map { it.toLocal(page = 0, total = 0) })
        }
        return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
    }
}
