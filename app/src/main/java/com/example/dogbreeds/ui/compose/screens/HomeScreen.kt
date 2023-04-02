package com.example.dogbreeds.ui.compose.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dogbreeds.R
import com.example.dogbreeds.ui.compose.composables.Breeds
import com.example.dogbreeds.ui.compose.composables.Search
import com.example.dogbreeds.ui.launchBreedDetailsActivity
import com.example.dogbreeds.viewmodels.BreedsViewModel
import com.example.dogbreeds.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel

const val DOG_BREED_ROUTE = "dog-breeds"
const val SEARCH_ROUTE = "search"

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Breeds : Screen(DOG_BREED_ROUTE, R.string.breeds)
    object Search : Screen(SEARCH_ROUTE, R.string.search)
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
fun BackAppBarButton(onClick: () -> Unit) = AppBarButton(
    onClick,
    resourceId = R.drawable.baseline_arrow_back_24,
    contentDescriptionId = R.string.content_description_back_button,
)

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
    val context = LocalContext.current

    val navController = rememberNavController()
    val homeState by homeViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        breedsViewModel.navigation.onEach {
            when(it) {
                is BreedsViewModel.Navigation.BreedDetailsScreen -> {
                    launchBreedDetailsActivity(context, it.id, it.name)
                }
            }
        }.launchIn(this)
    }

    var displayMode by remember { mutableStateOf(DisplayMode.GRID) }

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
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController,
                startDestination = Screen.Breeds.route,
            ) {
                composable(Screen.Breeds.route) {
                    Breeds(breedsViewModel, displayMode)
                }
                composable(Screen.Search.route) {
                    Search()
                }
            }
            AnimatedVisibility(
                visible = !homeState.hasNetwork,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Text(
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red),
                    text = "No network connection",
                )
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}