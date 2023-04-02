package com.example.dogbreeds.ui

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.example.dogbreeds.viewmodels.ViewModelEvent
import com.example.dogbreeds.viewmodels.ScreenNavigation

/**
 * TODO
 */
abstract class BaseFragment<N : ScreenNavigation, S, E : ViewModelEvent>(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    protected open fun onScreenNavigation(navigation: N) = Unit

    protected abstract fun onStateChanged(state: S)

    protected open fun onEvent(event: E) = Unit
}