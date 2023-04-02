package com.example.dogbreeds.data.datasources.remote

import com.example.dogbreeds.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class BreedDTO(
    val id: Int,
    val name: String,
    val image: Image,
) {
    @Serializable
    data class Image(
        val url: String,
    )
}

private const val DOG_API_END_POINT = "https://api.thedogapi.com/v1"

/**
 * Client that is responsible for making remote calls to the api
 */
class DogApiClient(engine: HttpClientEngine) {
    private val httpClient = HttpClient(engine) {
        expectSuccess = true
        defaultRequest {
            headers {
                append("X-Api-Key", BuildConfig.DOG_API_KEY)
            }
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    /**
     * TODO
     */
    suspend fun getBreeds(limit: Int, page: Int) = httpClient.get("$DOG_API_END_POINT/breeds") {
        url {
            with(parameters) {
                append("limit", "$limit")
                append("page", "$page")
            }
        }
    }
}