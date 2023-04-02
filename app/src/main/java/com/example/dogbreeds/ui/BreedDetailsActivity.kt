package com.example.dogbreeds.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import com.example.dogbreeds.BuildConfig
import com.example.dogbreeds.databinding.ActivityHomeBinding
import com.example.dogbreeds.ui.compose.screens.BreedDetailsScreen
import com.example.dogbreeds.viewmodels.BreedDetailsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * TODO
 */
class BreedDetailsActivity : AppCompatActivity() {
    companion object {
        const val BREED_ID = "BREED_ID"
        const val NAME = "NAME"
    }

    private val breedDetailsViewModel by viewModel<BreedDetailsViewModel> {
        val breedId = intent.extras?.getInt(BREED_ID) ?: throw IllegalArgumentException("Value for breed id is not in extras")
        val name = intent.extras?.getString(NAME) ?: throw IllegalArgumentException("Value for name id is not in extras")

        parametersOf(breedId, name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityHomeBinding.inflate(layoutInflater)

        when(BuildConfig.UI) {
            "VIEWS" -> setContentView(binding.root)
            "COMPOSE" -> {
                setContent {
                    MaterialTheme {
                        BreedDetailsScreen(
                            breedDetailsViewModel,
                            onBack = { finish() },
                        )
                    }
                }
            }
        }
    }
}