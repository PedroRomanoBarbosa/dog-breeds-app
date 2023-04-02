package com.example.dogbreeds.data.repositories

import com.example.dogbreeds.data.datasources.persistence.AppDatabase
import com.example.dogbreeds.data.datasources.remote.DogApiClient
import com.example.domain.Breed

/**
 * TODO
 */
const val LIMIT = 20

interface IBreedsRepository {
    fun getBreedPage(page: Int): List<Breed>
}

class BreedsRepository(
    private val api: DogApiClient,
    private val database: AppDatabase,
) : IBreedsRepository {
    companion object {
        const val TAG = "BreedsRepository"
    }

    override fun getBreedPage(page: Int): List<Breed> {
        TODO("Not yet implemented")
    }
}