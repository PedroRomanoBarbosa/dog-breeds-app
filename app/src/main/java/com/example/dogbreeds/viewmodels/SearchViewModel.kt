package com.example.dogbreeds.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.NetworkRepository
import com.example.dogbreeds.data.repositories.BreedsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

const val DEBOUNCE_DELAY_MS = 500L

/**
 * TODO
 */
@OptIn(FlowPreview::class)
class SearchViewModel(
    private val breedsRepository: BreedsRepository,
    networkRepository: NetworkRepository,
) : BaseViewModel<SearchViewModel.State, SearchViewModel.Navigation, SearchViewModel.Event>(
    initialState = State(hasNetwork = networkRepository.networkAvailable.value),
    tag = "SearchViewModel"
) {
    private var searchJob: Job? = null

    private val _textSearch = MutableStateFlow("")

    init {
        _textSearch.onEach {
            if (it.isEmpty()) {
                _state.update { state -> state.copy(text = it, searchBreedItems = emptyList()) }
            } else {
                _state.update { state -> state.copy(text = it) }
            }
        }.launchIn(viewModelScope)

        _textSearch.debounce(DEBOUNCE_DELAY_MS).onEach {
            _state.update { state -> state.copy(query = it) }

            search(it)
        }.launchIn(viewModelScope)

        networkRepository.networkAvailable.onEach {
            _state.update { state -> state.copy(hasNetwork = it) }
        }.launchIn(viewModelScope)
    }

    private fun search(term: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val breeds = breedsRepository.searchBreedsByTerm(term)

            _state.update { state ->
                state.copy(
                    searchBreedItems = breeds.map { breed ->
                        SearchItem(
                            id = breed.id,
                            title = breed.name,
                            details = buildList {
                                breed.category?.let { add(Detail.Category(it)) }
                                breed.origin?.let { add(Detail.Origin(it)) }
                            },
                        )
                    }
                )
            }
        }
    }

    fun onSearchBreedClick(breedId: Int, name: String) {
        viewModelScope.launch {
            _navigation.emit(Navigation.BreedDetailsScreen(id = breedId, name))
        }
    }

    fun setSearchText(it: String) {
        _textSearch.value = it
    }

    sealed class Detail(open val value: String) {
        data class Category(override val value: String) : Detail(value)
        data class Origin(override val value: String) : Detail(value)
    }

    data class SearchItem(
        val id: Int,
        val title: String,
        val details: List<Detail>,
    )

    /**
     * TODO
     */
    data class State(
        val hasNetwork: Boolean,
        val text: String = String(),
        val query: String = String(),
        val loading: Boolean = true,
        val searchBreedItems: List<SearchItem> = emptyList(),
    )

    /**
     * TODO
     */
    sealed interface Navigation : ScreenNavigation {
        data class BreedDetailsScreen(val id: Int, val name: String) : Navigation
    }

    /**
     * TODO
     */
    enum class Event : ViewModelEvent {
        SEARCH_FAILED
    }
}