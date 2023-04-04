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
 * Breed Details View Model
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
        Log.d(tag, "Init")

        networkRepository.networkAvailable.onEach { hasNetwork ->
            Log.d(tag, "hasNetwork=$hasNetwork")

            _state.update { it.copy(hasNetwork = hasNetwork) }
        }.launchIn(viewModelScope)

        Log.d(tag, "Loading breed with breedId=$breedId")

        viewModelScope.launch {
            val breed = runCatching {
                breedsRepository.getBreedById(breedId)
            }.getOrElse {
                Log.e(tag, "Error occurred when retrieving breed by id=$breedId", it)

                _event.emit(Event.BREED_LOAD_FAILED)

                return@launch
            }

            Log.e(tag, "Breed loading successful. breed=$breedId")

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
     * Represents a detail section in the screen. Can be [Loading] or [Details]
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
     * Breed Details Navigation
     */
    sealed interface Navigation : ScreenNavigation

    /**
     * Breed Details Event
     */
    enum class Event : ViewModelEvent {
        BREED_LOAD_FAILED
    }
}