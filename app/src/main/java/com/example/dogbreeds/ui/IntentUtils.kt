package com.example.dogbreeds.ui

import android.content.Context
import android.content.Intent
import com.example.dogbreeds.ui.BreedDetailsActivity.Companion.BREED_ID

/**
 * TODO
 */
fun launchBreedDetailsActivity(context: Context, breedId: Int) {
    Intent(context, BreedDetailsActivity::class.java).run {
        putExtra(BREED_ID, breedId)
    }.also {
        context.startActivity(it)
    }
}