package com.example.dogbreeds.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.NetworkRepository
import com.example.dogbreeds.data.repositories.BreedsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Breeds View Model
 */
class BreedsViewModel(
    private val breedsRepository: BreedsRepository,
    networkRepository: NetworkRepository,
) : BaseViewModel<BreedsViewModel.State, BreedsViewModel.Navigation, BreedsViewModel.Event>(
    initialState = State(networkRepository.networkAvailable.value),
    tag = "DogBreedsViewModel"
) {
    private var pageLoadJob: Job? = null

    init {
        Log.d(tag, "Init")

        networkRepository.networkAvailable.onEach {
            Log.d(tag, "hasNetwork=$it")

            _state.update { state -> state.copy(hasNetwork = it) }
        }.launchIn(viewModelScope)

        val pageIndex = _state.value.currentPageIndex

        loadPage(pageIndex)
    }

    private fun loadPage(pageIndex: Int, refresh: Boolean = false) {
        Log.d(tag, "Loading page for pageIndex=$pageIndex with refresh=$refresh")

        pageLoadJob?.cancel()

        _state.update {
            if (refresh) it.copy(refreshing = true)
            else it.copy(loading = true)
        }

        viewModelScope.launch {
            pageLoadJob = breedsRepository.getBreedPage(pageIndex, refresh).onEach { result ->
                val page = result.getOrElse {
                    Log.e(tag, "Loading page failed", it)

                    _event.emit(Event.FAILED_TO_LOAD)

                    return@onEach
                }

                Log.d(tag, "Loading page=$page")

                _state.update { state ->
                    state.copy(
                        previousEnabled = page.hasPrev,
                        nextEnabled = page.hasNext,
                        loading = false,
                        refreshing = false,
                        currentPageIndex = pageIndex,
                        totalPages = page.totalPages,
                        breedItems = page.breeds.map {
                            it?.let {
                                BreedItem(
                                    id = it.id,
                                    label = it.name,
                                    imageUrl = it.imageUrl,
                                )
                            }
                        }
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onBreedClick(breedId: Int, name: String) {
        Log.d(tag, "Breed with breedId=$breedId and name=$name was clicked")

        viewModelScope.launch {
            _navigation.emit(Navigation.BreedDetailsScreen(id = breedId, name))
        }
    }

    fun selectPage(pageIndex: Int) {
        Log.d(tag, "Selecting page with pageIndex=$pageIndex")

        // TODO

        loadPage(pageIndex)
    }

    fun nextPage() {
        Log.d(tag, "Loading next page")

        val currentPageIndex = _state.value.currentPageIndex
        val nextPageIndex = currentPageIndex + 1

        loadPage(nextPageIndex)
    }

    fun previousPage() {
        Log.d(tag, "Loading previous page")

        val currentPageIndex = _state.value.currentPageIndex
        val nextPageIndex = currentPageIndex - 1

        loadPage(nextPageIndex)
    }

    fun refreshPage() {
        Log.d(tag, "Refreshing page")

        val pageIndex = _state.value.currentPageIndex

        loadPage(pageIndex, refresh = true)
    }

    data class BreedItem(
        val id: Int,
        val label: String,
        val imageUrl: String?,
    )

    /**
     * Breeds State
     */
    data class State(
        val hasNetwork: Boolean,
        val refreshing: Boolean = false,
        val loading: Boolean = true,
        val currentPageIndex: Int = 0,
        val totalPages: Int? = 0,
        val previousEnabled: Boolean = false,
        val nextEnabled: Boolean = true,
        val breedItems: List<BreedItem?> = emptyList(),
    )

    /**
     * Breeds Navigation
     */
    sealed interface Navigation : ScreenNavigation {
        data class BreedDetailsScreen(val id: Int, val name: String) : Navigation
    }

    /**
     * Breeds Event
     */
    enum class Event : ViewModelEvent {
        FAILED_TO_LOAD
    }
}