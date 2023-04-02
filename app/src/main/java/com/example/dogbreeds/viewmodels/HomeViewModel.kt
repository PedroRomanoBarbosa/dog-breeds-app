package com.example.dogbreeds.viewmodels

/**
 * TODO
 */
class HomeViewModel : BaseViewModel<HomeViewModel.State, HomeViewModel.Navigation, HomeViewModel.Event>(
    initialState = State(),
    tag = "HomeViewModel"
) {
    /**
     * TODO
     */
    data class State(
        val breeds: List<String> = emptyList(),
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