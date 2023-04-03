package com.example.dogbreeds.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import com.example.dogbreeds.R
import com.example.dogbreeds.databinding.FragmentDogBreedsBinding
import com.example.dogbreeds.viewmodels.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Home Fragment
 */
class HomeFragment : BaseFragment<HomeViewModel.Navigation, HomeViewModel.State, HomeViewModel.Event>(R.layout.fragment_home) {
    private val binding by lazy { FragmentDogBreedsBinding.inflate(layoutInflater) }

    private val homeViewModel by viewModel<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
    }

    private fun setupUi() {
        with(binding) {
            // TODO
        }

        with(homeViewModel) {
            state.asLiveData().observe(viewLifecycleOwner, ::onStateChanged)
            navigation.asLiveData().observe(viewLifecycleOwner, ::onScreenNavigation)
            event.asLiveData().observe(viewLifecycleOwner, ::onEvent)
        }
    }

    override fun onStateChanged(state: HomeViewModel.State) {
        with(binding) {

        }
    }
}