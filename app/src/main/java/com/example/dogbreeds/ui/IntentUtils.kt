package com.example.dogbreeds.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.dogbreeds.ui.BreedDetailsActivity.Companion.BREED_ID
import com.example.dogbreeds.ui.BreedDetailsActivity.Companion.NAME

/**
 * Launches [BreedDetailsActivity] with a given [breedId] and [name]
 */
fun launchBreedDetailsActivity(context: Context, breedId: Int, name: String) {
    Intent(context, BreedDetailsActivity::class.java).run {
        putExtra(BREED_ID, breedId)
        putExtra(NAME, name)
    }.also {
        context.startActivity(it)
    }
}

/**
 * Launches the default network settings activity
 */
fun launchNetworkSettings(context: Context) {
    ContextCompat.startActivity(
        context,
        Intent(Settings.ACTION_WIRELESS_SETTINGS),
        null,
    )
}