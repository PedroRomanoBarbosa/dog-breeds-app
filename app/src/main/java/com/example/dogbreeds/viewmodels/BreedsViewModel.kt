package com.example.dogbreeds.viewmodels

import com.example.dogbreeds.data.datasources.persistence.AppDatabase
import com.example.dogbreeds.data.datasources.remote.DogApiClient

/**
 * TODO
 */
class BreedsViewModel(
    private val api: DogApiClient,
    private val database: AppDatabase,
) : BaseViewModel<BreedsViewModel.State, BreedsViewModel.Navigation, BreedsViewModel.Event>(
    initialState = State(),
    tag = "DogBreedsViewModel"
) {
    init {

    }

    fun loadCurrentPage(index: Int) {

    }

    fun onBreedClick(breedId: Int) {

    }

    fun selectPage(pageIndex: Int) {

    }

    fun nextPage() {

    }

    fun previousPage() {

    }

    /**
     * TODO
     */
    data class BreedItem(
        val id: Int,
        val label: String,
        val imageUrl: String,
    )

    /**
     * TODO
     */
    data class State(
        val currentPageIndex: Int = 0,
        val totalPages: Int? = 0,
        val breeds: List<BreedItem> = emptyList(),
    )

    /**
     * TODO
     */
    sealed interface Navigation : ScreenNavigation

    /**
     * TODO
     */
    enum class Event : ViewModelEvent
}