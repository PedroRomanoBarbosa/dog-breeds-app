package com.example.dogbreeds.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dogbreeds.BuildConfig
import com.example.dogbreeds.databinding.ActivityHomeBinding
import com.example.dogbreeds.ui.compose.screens.HomeScreen
import com.example.dogbreeds.viewmodels.BreedsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * TODO
 */
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityHomeBinding.inflate(layoutInflater)

        when(BuildConfig.UI) {
            "VIEWS" -> setContentView(binding.root)
            "COMPOSE" -> {
                setContent {
                    MaterialTheme {
                        HomeScreen()
                    }
                }
            }
        }
    }
}