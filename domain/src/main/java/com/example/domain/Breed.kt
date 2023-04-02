package com.example.domain

/**
 * TODO
 */
data class Breed(
    val id: Int,
    val name: String,
    val imageUrl: String? = null,
    val category: String? = null,
    val origin: String? = null,
    val temperament: String? = null,
)