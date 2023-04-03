package com.example.dogbreeds

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
 * TODO
 */
fun buildLabelText(label: String, text: String) = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append(label)
    }
    append(text)
}

/**
 * TODO
 */
suspend fun enableRequestDelay() {
    if (REQUEST_DELAY_ENABLED) delay(REQUEST_DELAY)
}