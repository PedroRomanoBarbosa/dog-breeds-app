package com.example.dogbreeds.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.NetworkRepository
import com.example.dogbreeds.data.repositories.BreedsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * TODO
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
        networkRepository.networkAvailable.onEach {
            _state.update { state -> state.copy(hasNetwork = it) }
        }.launchIn(viewModelScope)

        val pageIndex = _state.value.currentPageIndex

        loadPage(pageIndex)
    }

    private fun loadPage(pageIndex: Int, refresh: Boolean = false) {
        pageLoadJob?.cancel()

        _state.update {
            if (refresh) it.copy(refreshing = true)
            else it.copy(loading = true)
        }

        viewModelScope.launch {
            pageLoadJob = breedsRepository.getBreedPage(pageIndex, refresh).onEach { result ->
                result.getOrNull()?.let { page ->
                    _state.update { state ->
                        state.copy(
                            previousEnabled = page.hasPrev,
                            nextEnabled = page.hasNext,
                            loading = false,
                            refreshing = false,
                            currentPageIndex = pageIndex,
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
                } ?: run {
                    _event.emit(Event.FAILED_TO_LOAD)
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onBreedClick(breedId: Int) {
        viewModelScope.launch {
            _navigation.emit(Navigation.BreedDetailsScreen(id = breedId))
        }
    }

    fun selectPage(pageIndex: Int) {
        loadPage(pageIndex)
    }

    fun nextPage() {
        val currentPageIndex = _state.value.currentPageIndex
        val nextPageIndex = currentPageIndex + 1

        loadPage(nextPageIndex)
    }

    fun previousPage() {
        val currentPageIndex = _state.value.currentPageIndex
        val nextPageIndex = currentPageIndex - 1

        loadPage(nextPageIndex)
    }

    fun refreshPage() {
        val pageIndex = _state.value.currentPageIndex

        loadPage(pageIndex, refresh = true)
    }

    data class BreedItem(
        val id: Int,
        val label: String,
        val imageUrl: String,
    )

    /**
     * TODO
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
     * TODO
     */
    sealed interface Navigation : ScreenNavigation {
        data class BreedDetailsScreen(val id: Int) : Navigation
    }

    /**
     * TODO
     */
    enum class Event : ViewModelEvent {
        FAILED_TO_LOAD
    }
}