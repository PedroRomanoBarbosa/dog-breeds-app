package com.example.domain

/**
 * Domain object that represents a dog breed and has all the information related to it
 */
data class Breed(
    val id: Int,
    val name: String,
    val imageUrl: String? = null,
    val category: String? = null,
    val origin: String? = null,
    val temperament: String? = null,
)