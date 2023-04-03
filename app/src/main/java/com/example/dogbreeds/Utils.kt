package com.example.dogbreeds

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.dogbreeds.BuildConfig.REQUEST_DELAY_ENABLED
import com.example.dogbreeds.Configuration.REQUEST_DELAY
import com.example.dogbreeds.data.datasources.persistence.BreedLocal
import com.example.dogbreeds.data.datasources.remote.BreedDTO
import com.example.domain.Breed
import kotlinx.coroutines.delay

fun BreedLocal.toDomain() = Breed(
    id = id,
    name = name,
    imageUrl = imageUrl,
    category = category,
    origin = origin,
    temperament = temperament,
)

fun BreedDTO.toDomain() = Breed(
    id = id,
    name = name,
    imageUrl = image?.url,
    category = breed_group,
    origin = origin,
    temperament = temperament,
)

fun BreedDTO.toLocal(page: Int, total: Int) = BreedLocal(
    id = id,
    name = name,
    imageUrl = image?.url,
    category = breed_group,
    origin = origin,
    temperament = temperament,
    page,
    total,
)

/**
 * Builds an [AnnotatedString] with a [label] in bold and the [text]
 */
fun buildLabelText(label: String, text: String): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append(label)
    }
    append(text)
}

/**
 * Checks whether the mock delay [BuildConfig.REQUEST_DELAY_ENABLED] is enabled and delays the
 * coroutine by [REQUEST_DELAY]
 */
suspend fun enableRequestDelay() {
    if (REQUEST_DELAY_ENABLED) delay(REQUEST_DELAY)
}

/**
 * Calculates the total number of pages given a [total] and a [limit]
 */
fun calculateTotalPages(total: Int, limit: Int): Int {
    var num = total / limit
    if (total % limit > 0) num++
    return num
}

/**
 * Check if there is a next page iven a [pageIndex], [limit] and a [total]
 */
fun hasNextPage(pageIndex: Int, limit: Int, total: Int) = (pageIndex + 1) * limit < total