package com.example.dogbreeds.viewmodels

class BreedDetailsViewModel : BaseViewModel<BreedDetailsViewModel.State, BreedDetailsViewModel.Navigation, BreedDetailsViewModel.Event>(
    initialState = State(true),
    tag = "BreedDetailsViewModel"
) {
    /**
     * TODO
     */
    data class State(
        val loading: Boolean,
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