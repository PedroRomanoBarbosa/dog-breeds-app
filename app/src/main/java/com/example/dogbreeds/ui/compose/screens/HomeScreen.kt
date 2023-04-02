package com.example.dogbreeds.ui.compose.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import com.example.dogbreeds.R
import com.example.dogbreeds.viewmodels.BreedsViewModel
import com.example.dogbreeds.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

const val DOG_BREED_ROUTE = "dog-breeds"
const val SEARCH_ROUTE = "search"

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Breeds : Screen(DOG_BREED_ROUTE, R.string.breeds)
    object Search : Screen(SEARCH_ROUTE, R.string.search)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Breeds(
    breedsViewModel: BreedsViewModel,
    displayMode: DisplayMode,
) {
    val breedsState = breedsViewModel.state.collectAsState()
    val breeds = breedsState.value.breeds

    val (gridCellsNumber, itemHeight) = when(displayMode) {
        DisplayMode.LIST -> 1 to 200.dp
        DisplayMode.GRID -> 2 to 150.dp
    }

    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    fun refresh() = refreshScope.launch {
        refreshing = true
        delay(1500)
        refreshing = false
    }
    val refreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refresh()
        },
    )

    Box(modifier = Modifier.pullRefresh(refreshState)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridCellsNumber),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxHeight(),
            userScrollEnabled = !refreshing,
        ) {
            items(items = breeds, key = { }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Box {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            model = it.imageUrl,
                            contentDescription = "breed ${it.label}",
                        )
                        Box(modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.DarkGray, shape = RoundedCornerShape(16.dp))
                            .padding(8.dp)) {
                            Text(color = Color.White, text = it.label)
                        }
                    }
                }
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search() {
    TextField(value = "Search", onValueChange = {})
}

@Composable
fun AppBarButton(
    onClick: () -> Unit,
    @DrawableRes resourceId: Int,
    @StringRes contentDescriptionId: Int,
) {
    IconButton(onClick = onClick) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = stringResource(id = contentDescriptionId),
        )
    }
}

@Composable
fun ListViewAppBarButton(onClick: () -> Unit) = AppBarButton(
    onClick,
    resourceId = R.drawable.round_table_rows_24,
    contentDescriptionId = R.string.content_description_list_view_button,
)

@Composable
fun GridViewAppBarButton(onClick: () -> Unit) = AppBarButton(
    onClick,
    resourceId = R.drawable.round_grid_view_24,
    contentDescriptionId = R.string.content_description_grid_view_button,
)

/**
 * TODO
 */
enum class DisplayMode {
    LIST,
    GRID,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = koinViewModel(),
    breedsViewModel: BreedsViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val homeState by homeViewModel.state.collectAsState()
    val breedsState by breedsViewModel.state.collectAsState()

    var displayMode by remember { mutableStateOf(DisplayMode.LIST) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    ListViewAppBarButton { displayMode = DisplayMode.LIST }
                    GridViewAppBarButton { displayMode = DisplayMode.GRID }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Breeds.route } == true,
                    onClick = {
                        navController.navigate(Screen.Breeds.route)
                    },
                    label = { Text(text = "Breeds") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Search.route } == true,
                    onClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    label = { Text(text = "Search") },
                    icon = { Icon(Icons.Default.Search, contentDescription = "") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Breeds.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Breeds.route) {
                Breeds(breedsViewModel, displayMode)
            }
            composable(Screen.Search.route) {
                Search()
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}