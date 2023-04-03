package com.example.dogbreeds.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base Screen Navigation
 */
sealed interface ScreenNavigation {
    object Back : ScreenNavigation
}

/**
 * Base View Model Event
 */
interface ViewModelEvent

/**
 * Base View Model
 */
abstract class BaseViewModel<S, N : ScreenNavigation, E : ViewModelEvent?>(
    initialState: S,
    protected val tag: String,
) : ViewModel() {
    protected val _navigation = MutableSharedFlow<N>()
    val navigation = _navigation.asSharedFlow()

    protected val _state = MutableStateFlow(initialState)
    open val state = _state.asStateFlow()

    protected val _event = MutableSharedFlow<E>()
    val event = _event.asSharedFlow()

    protected fun log(message: String) {
        Log.d(tag, message)
    }
}