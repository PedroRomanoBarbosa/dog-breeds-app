package com.example.dogbreeds.ui.compose.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dogbreeds.R
import com.example.dogbreeds.viewmodels.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    searchViewModel: SearchViewModel = koinViewModel(),
) {
    val state = searchViewModel.state.collectAsState()
    val searchItems = state.value.searchBreedItems
    val loading = state.value.loading
    val text = state.value.text
    val query = state.value.query
    val noItems = state.value.searchBreedItems.isEmpty()

    Column(Modifier.padding(start = 16.dp, end = 16.dp)) {
        TextField(
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = { searchViewModel.clearSearch() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text(text = "Search for a breed name") },
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { searchViewModel.setSearchText(it) },
        )

        if (query.isNotEmpty() && !loading && noItems) {
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_error_24),
                        contentDescription = "",
                    )
                    Text(textAlign = TextAlign.Center, text = "There are no dog breeds whose name matches this query")
                }
            }
        }
        
        if (!noItems) {
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
                                val text = when(detail) {
                                    is SearchViewModel.Detail.Category -> "Category"
                                    is SearchViewModel.Detail.Origin -> "Origin"
                                }
                                Text("$text: ${detail.value}")
                            }
                        }
                    }
                }
            }
        }
    }
}