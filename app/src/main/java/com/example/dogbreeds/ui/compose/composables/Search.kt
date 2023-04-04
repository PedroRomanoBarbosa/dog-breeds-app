package com.example.dogbreeds.ui.compose.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dogbreeds.R
import com.example.dogbreeds.ui.launchNetworkSettings
import com.example.dogbreeds.viewmodels.SearchViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    searchViewModel: SearchViewModel = koinViewModel(),
) {
    val context = LocalContext.current

    val state = searchViewModel.state.collectAsState()
    val searchItems = state.value.searchBreedItems
    val loading = state.value.loading
    val text = state.value.text
    val query = state.value.query
    val noItems = state.value.searchBreedItems.isEmpty()

    // Snackbar setup
    // TODO Extract the whole snackbar composables and logic as its own composable
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        searchViewModel.event.onEach {
            when(it) {
                SearchViewModel.Event.SEARCH_FAILED -> {
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

    Box {
        Column(Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp)) {
            TextField(
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { searchViewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.content_description_clear_search),
                            )
                        }
                    }
                },
                placeholder = { Text(text = stringResource(R.string.search_placeholder_text)) },
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = { searchViewModel.setSearchText(it) },
            )

            when {
                loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        CircularProgressIndicator(
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }

                query.isNotBlank() && noItems -> {
                    InfoSection(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        iconRes = R.drawable.baseline_error_24,
                        messageRes = R.string.search_empty,
                        contentDescriptionRes = R.string.content_description_warning_icon,
                    )
                }

                text.isBlank() -> {
                    InfoSection(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        iconRes = R.drawable.baseline_search_24,
                        messageRes = R.string.start_search,
                    )
                }

                !noItems -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(searchItems) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp, top = 8.dp),
                                onClick = { searchViewModel.onSearchBreedClick(item.id, item.title) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = item.title, fontWeight = FontWeight.Bold)
                                    for (detail in item.details) {
                                        val value = when(detail) {
                                            is SearchViewModel.Detail.Category -> stringResource(R.string.category)
                                            is SearchViewModel.Detail.Origin -> stringResource(R.string.origin)
                                        }
                                        Text("$value: ${detail.value}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            hostState = snackbarHostState
        )
    }
}