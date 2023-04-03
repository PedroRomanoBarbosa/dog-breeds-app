package com.example.dogbreeds.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.NetworkRepository
import com.example.dogbreeds.data.repositories.BreedsRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * TODO
 */
class BreedDetailsViewModel(
    breedId: Int,
    name: String,
    breedsRepository: BreedsRepository,
    networkRepository: NetworkRepository,
) : BaseViewModel<BreedDetailsViewModel.State, BreedDetailsViewModel.Navigation, BreedDetailsViewModel.Event>(
    initialState = State(
        name = name,
        hasNetwork = networkRepository.networkAvailable.value,
    ),
    tag = "BreedDetailsViewModel"
) {
    init {
        networkRepository.networkAvailable.onEach { hasNetwork ->
            _state.update { it.copy(hasNetwork = hasNetwork) }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            val breed = runCatching {
                breedsRepository.getBreedById(breedId)
            }.onFailure {
                Log.e(tag, "Error occurred when retrieving breed by id=$breedId", it)
            }.getOrNull() ?: return@launch

            with(breed) {
                _state.update {
                    it.copy(
                        detailsSection = DetailsSection.Details(
                            imageUrl,
                            category,
                            origin,
                            temperament
                        )
                    )
                }
            }
        }
    }

    data class State(
        val name: String,
        val hasNetwork: Boolean,
        val detailsSection: DetailsSection = DetailsSection.Loading,
    )

    /**
     * TODO
     */
    sealed interface DetailsSection {
        object Loading : DetailsSection

        data class Details(
            val imageUrl: String?,
            val category: String?,
            val origin: String?,
            val temperament: String?,
        ) : DetailsSection
    }

    /**
     * TODO
     */
    sealed interface Navigation : ScreenNavigation

    /**
     * TODO
     */
    enum class Event : ViewModelEvent
}