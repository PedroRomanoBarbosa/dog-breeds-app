package com.example.dogbreeds.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.NetworkRepository
import com.example.dogbreeds.data.repositories.BreedsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

const val DEBOUNCE_DELAY_MS = 500L

/**
 * Search View Model
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

    private val _textSearch = MutableStateFlow(String())

    init {
        Log.d(tag, "Init")

        _textSearch.onEach {
            _state.update { state ->
                state.copy(
                    text = it,
                    query = String(),
                    loading = it.isNotEmpty(),
                    searchBreedItems = if (it.isEmpty()) emptyList() else state.searchBreedItems,
                )
            }
        }.launchIn(viewModelScope)

        _textSearch.debounce(DEBOUNCE_DELAY_MS).onEach {
            Log.d(tag, "debouncedText=$it")

            if (it.isNotBlank()) {
                _state.update { state -> state.copy(query = it) }

                search(it)
            }
        }.launchIn(viewModelScope)

        networkRepository.networkAvailable.onEach {
            Log.d(tag, "hasNetwork=$it")

            _state.update { state -> state.copy(hasNetwork = it) }
        }.launchIn(viewModelScope)
    }

    private fun search(term: String) {
        Log.d(tag, "Start searching for term=$term")

        _state.update { it.copy(loading = true) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val result = breedsRepository.searchBreedsByTerm(term)

            val breeds = result.getOrElse {
                Log.e(tag, "Occurred an error when searching for breeds", it)

                _event.emit(Event.SEARCH_FAILED)
                _state.update { state -> state.copy(loading = false) }

                return@launch
            }

            Log.d(tag, "search successful. breedsTotal=${breeds.size}")

            _state.update { state ->
                state.copy(
                    loading = false,
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

    fun clearSearch() {
        Log.d(tag, "Clearing search")

        _state.update {
            it.copy(
                searchBreedItems = emptyList(),
                text = String(),
                query = String(),
            )
        }
    }

    fun onSearchBreedClick(breedId: Int, name: String) {
        Log.d(tag, "Breed with breedId=$breedId and name=$name was clicked")

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
     * Search State
     */
    data class State(
        val hasNetwork: Boolean,
        val text: String = String(),
        val query: String = String(),
        val loading: Boolean = false,
        val searchBreedItems: List<SearchItem> = emptyList(),
    )

    /**
     * Search Navigation
     */
    sealed interface Navigation : ScreenNavigation {
        data class BreedDetailsScreen(val id: Int, val name: String) : Navigation
    }

    /**
     * Search Event
     */
    enum class Event : ViewModelEvent {
        SEARCH_FAILED
    }
}