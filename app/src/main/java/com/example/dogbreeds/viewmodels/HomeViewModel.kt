package com.example.dogbreeds.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.dogbreeds.NetworkRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * Home View Model
 */
class HomeViewModel(
    networkRepository: NetworkRepository,
) : BaseViewModel<HomeViewModel.State, HomeViewModel.Navigation, HomeViewModel.Event>(
    initialState = State(networkRepository.networkAvailable.value),
    tag = "HomeViewModel"
) {
    init {
        networkRepository.networkAvailable.onEach { hasNetwork ->
            Log.d(tag, "hasNetwork=$hasNetwork")

            _state.update { it.copy(hasNetwork = hasNetwork) }
        }.launchIn(viewModelScope)
    }

    /**
     * Home State
     */
    data class State(
        val hasNetwork: Boolean,
    )

    /**
     * Home Navigation
     */
    sealed interface Navigation : ScreenNavigation

    /**
     * Home Event
     */
    enum class Event : ViewModelEvent
}