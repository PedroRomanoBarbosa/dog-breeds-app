package com.example.dogbreeds.ui.compose.composables

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dogbreeds.R
import com.example.dogbreeds.ui.compose.screens.DisplayMode
import com.example.dogbreeds.ui.launchNetworkSettings
import com.example.dogbreeds.viewmodels.BreedsViewModel
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Breeds(
    breedsViewModel: BreedsViewModel,
    displayMode: DisplayMode,
) {
    val context = LocalContext.current

    val breedsState = breedsViewModel.state.collectAsState()
    val breeds = breedsState.value.breedItems
    val loading = breedsState.value.loading
    val refreshing = breedsState.value.refreshing
    val hasNetwork = breedsState.value.hasNetwork

    // Snackbar setup
    // TODO Extract the whole snackbar composables and logic as its own composable
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        breedsViewModel.event.onEach {
            when(it) {
                BreedsViewModel.Event.FAILED_TO_LOAD -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.error_occurred),
                        actionLabel = context.getString(R.string.open),
                        duration = SnackbarDuration.Short,
                    )

                    if (result == SnackbarResult.ActionPerformed) launchNetworkSettings(context)
                }
            }
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
                contentPadding = PaddingValues(top = 100.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
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
                            .then(if (loading || refreshing) Modifier.shimmer() else Modifier),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        enabled = breedItem != null,
                        onClick = {
                            breedItem?.let {
                                breedsViewModel.onBreedClick(it.id, it.label)
                            }
                        },
                    ) {
                        Box {
                            AsyncImage(
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                model = breedItem?.imageUrl,
                                contentDescription =  stringResource(
                                    id = R.string.content_description_breed_image,
                                    breedItem?.label ?: String(),
                                ),
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
                                    Text(String())
                                }
                            }
                        }
                    }
                }
            }
        }

        with(breedsState.value) {
            PageControlPanel(
                currentPageIndex,
                totalPages,
                previousEnabled,
                nextEnabled,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp),
                onNext = { breedsViewModel.nextPage() },
                onPrevious = { breedsViewModel.previousPage() },
            )
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

        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            hostState = snackbarHostState
        )
    }
}