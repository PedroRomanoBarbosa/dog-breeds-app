package com.example.dogbreeds.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.NetworkRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * TODO
 */
class HomeViewModel(
    networkRepository: NetworkRepository,
) : BaseViewModel<HomeViewModel.State, HomeViewModel.Navigation, HomeViewModel.Event>(
    initialState = State(networkRepository.networkAvailable.value),
    tag = "HomeViewModel"
) {
    init {
        networkRepository.networkAvailable.onEach { hasNetwork ->
            _state.update { it.copy(hasNetwork = hasNetwork) }
        }.launchIn(viewModelScope)
    }

    /**
     * TODO
     */
    data class State(
        val hasNetwork: Boolean,
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