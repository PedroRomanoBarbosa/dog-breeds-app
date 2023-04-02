package com.example.dogbreeds.ui.compose.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dogbreeds.ui.compose.screens.DisplayMode
import com.example.dogbreeds.viewmodels.BreedsViewModel
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Breeds(
    breedsViewModel: BreedsViewModel,
    displayMode: DisplayMode,
) {
    val breedsState = breedsViewModel.state.collectAsState()
    val breeds = breedsState.value.breedItems
    val loading = breedsState.value.loading
    val refreshing = breedsState.value.refreshing
    val hasNetwork = breedsState.value.hasNetwork

    // Snackbar setup
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        breedsViewModel.event.onEach {
            snackbarHostState.showSnackbar("Hello there")
        }.launchIn(scope)
    }

    val (gridCellsNumber, itemHeight) = when(displayMode) {
        DisplayMode.LIST -> 1 to 200.dp
        DisplayMode.GRID -> 2 to 150.dp
    }

    val refreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            breedsViewModel.refreshPage()
        },
    )

    Box(modifier = Modifier.pullRefresh(state = refreshState, enabled = hasNetwork)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyVerticalGrid(
                modifier = Modifier.weight(1f),
                columns = GridCells.Fixed(gridCellsNumber),
                contentPadding = PaddingValues(top = 64.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = !refreshing,
            ) {
                itemsIndexed(
                    items = breeds,
                    key = { index, breedItem -> breedItem?.let { "item-${it.id}" } ?: "placeholder-$index" },
                ) { _, breedItem ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .then(if (loading) Modifier.shimmer() else Modifier)
                            .clickable(enabled = breedItem != null) {
                                breedItem?.let {
                                    breedsViewModel.onBreedClick(it.id)
                                }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    ) {
                        Box {
                            AsyncImage(
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                model = breedItem?.imageUrl,
                                contentDescription = "breed ${breedItem?.label}",
                            )
                            breedItem?.let {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .background(
                                            Color.DarkGray,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(color = Color.White, text = breedItem.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            } ?: run {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .width(120.dp)
                                        .padding(8.dp)
                                        .background(
                                            Color.DarkGray,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text("")
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.align(Alignment.TopCenter)) {
            Button(enabled = breedsState.value.previousEnabled, onClick = { breedsViewModel.previousPage() }) {
                Text(text = "Prev")
            }
            Text(text = "Page: ${breedsState.value.currentPageIndex + 1}")
            Button(enabled = breedsState.value.nextEnabled, onClick = { breedsViewModel.nextPage() }) {
                Text(text = "Next")
            }
        }

        if (refreshing) Box(modifier = Modifier
            .fillMaxSize()
            .alpha(0.5f)
            .background(Color.LightGray))

        PullRefreshIndicator(
            refreshing,
            refreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        SnackbarHost(hostState = snackbarHostState)
    }
}