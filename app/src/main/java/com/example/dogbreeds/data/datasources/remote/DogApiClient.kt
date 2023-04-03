package com.example.dogbreeds.data.datasources.remote

import com.example.dogbreeds.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class BreedDTO(
    val id: Int,
    val name: String,
    val image: Image? = null,
    val temperament: String? = null,
    @SerialName("breed_group") // There was a bug when I used `breedGroup` for the name of he variable
    val breed_group: String? = null,
    val origin: String? = null,
    @SerialName("reference_image_id")
    val reference_image_id: String? = null,
) {
    @Serializable
    data class Image(
        val url: String,
    )
}

@Serializable
data class ImageDTO(
    val id: String,
    val url: String,
)

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
     * Fetches the breeds from the [page] with a given [limit]
     *
     * @param limit The number of maximum results per page
     * @param page The number of the page to fetch
     */
    suspend fun getBreeds(limit: Int, page: Int) = httpClient.get("$DOG_API_END_POINT/breeds") {
        url {
            with(parameters) {
                append("limit", "$limit")
                append("page", "$page")
            }
        }
    }

    /**
     * Search for all the breeds that have [term] in the name
     *
     * @param term The query for the search
     */
    suspend fun searchBreeds(term: String) = httpClient.get("$DOG_API_END_POINT/breeds/search") {
        url {
            with(parameters) {
                append("q", term)
            }
        }
    }

    /**
     * Fetches a breed by [breedId]
     */
    suspend fun getBreedById(breedId: Int) = httpClient.get("$DOG_API_END_POINT/breeds/$breedId")

    /**
     * Fetches an image by [imageId]
     */
    suspend fun getImageById(imageId: String) = httpClient.get("$DOG_API_END_POINT/images/$imageId")
}