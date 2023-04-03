package com.example.dogbreeds.ui.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dogbreeds.buildLabelText
import com.example.dogbreeds.viewmodels.BreedDetailsViewModel

@Composable
fun Details(
    loading: Boolean,
    name: String,
    imageUrl: String? = null,
    origin: String? = null,
    temperament: String? = null,
    category: String? = null,
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxWidth()
        ) {
            if (loading) {
                Box(modifier = Modifier.background(Color.LightGray).height(200.dp).fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                    )
                }
            } else {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = 200.dp),
                    contentScale = ContentScale.Crop,
                    model = imageUrl,
                    contentDescription = "",
                )
            }
        }

        Box(modifier = Modifier
            .offset(y = (-32).dp)
            .padding(start = 16.dp, end = 16.dp)
            .background(Color.DarkGray, shape = RoundedCornerShape(16.dp))
            .padding(8.dp)
        ) {
            Text(text = name, color = Color.White, fontSize = 24.sp)
        }

        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 64.dp)) {
            if (category?.isNotEmpty() == true) {
                Text(buildLabelText("Category: ", category), fontSize = 18.sp)
            }
            if (origin?.isNotEmpty() == true) {
                Text(buildLabelText("Origin: ", origin), fontSize = 18.sp)
            }
            if (temperament?.isNotEmpty() == true) {
                Text(buildLabelText("Temperament: ", temperament), fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedDetailsScreen(
    breedDetailsViewModel: BreedDetailsViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val breedDetailsState = breedDetailsViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackAppBarButton(onBack) },
                title = { Text(text = "${breedDetailsState.value.name} Details") },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            when (val state = breedDetailsState.value) {
                is BreedDetailsViewModel.State.Loading -> {
                    Details(name = state.name, loading = true)
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black,
                    )
                }
                is BreedDetailsViewModel.State.Details -> {
                    Details(
                        loading = false,
                        name = state.name,
                        imageUrl = state.imageUrl,
                        category = state.category,
                        origin = state.origin,
                        temperament = state.temperament,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BreedDetailsScreenPreview() {
    BreedDetailsScreen()
}