package com.example.dogbreeds.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.data.repositories.BreedsRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BreedDetailsViewModel(
    breedId: Int,
    name: String,
    breedsRepository: BreedsRepository,
) : BaseViewModel<BreedDetailsViewModel.State, BreedDetailsViewModel.Navigation, BreedDetailsViewModel.Event>(
    initialState = State.Loading(name),
    tag = "BreedDetailsViewModel"
) {
    init {
        viewModelScope.launch {
            val breed = breedsRepository.getBreedById(breedId)

            with(breed) {
                _state.update {
                    State.Details(
                        name = breed.name,
                        imageUrl,
                        category,
                        origin,
                        temperament
                    )
                }
            }
        }
    }

    /**
     * TODO
     */
    sealed class State(open val name: String) {
        data class Loading(override val name: String) : State(name)

        data class Details(
            override val name: String,
            val imageUrl: String?,
            val category: String?,
            val origin: String?,
            val temperament: String?,
        ) : State(name)
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