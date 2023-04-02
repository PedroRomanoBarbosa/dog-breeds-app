package com.example.dogbreeds.ui.compose.composables

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search() {
    TextField(value = "Search", onValueChange = {})
}