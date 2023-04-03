package com.example.dogbreeds

import androidx.room.Room
import coil.ImageLoader
import coil.disk.DiskCache
import com.example.dogbreeds.data.datasources.persistence.AppDatabase
import com.example.dogbreeds.data.datasources.remote.DogApiClient
import com.example.dogbreeds.data.repositories.BreedsRepository
import com.example.dogbreeds.viewmodels.BreedDetailsViewModel
import com.example.dogbreeds.viewmodels.BreedsViewModel
import com.example.dogbreeds.viewmodels.HomeViewModel
import com.example.dogbreeds.viewmodels.SearchViewModel
import io.ktor.client.engine.android.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val startModule = module(createdAtStart = true) {
    single {
        ImageLoader.Builder(androidContext())
            .crossfade(true)
            .diskCache {
                DiskCache.Builder().maxSizePercent(0.1).build()
            }
            .build()
    }
    single { NetworkRepository(androidContext(), CoroutineScope(Dispatchers.Default)) } bind INetworkRepository::class
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()
    }
    single { DogApiClient(AndroidClientEngine(AndroidEngineConfig())) }
}

val appModule = module {
    singleOf(::BreedsRepository)

    viewModelOf(::HomeViewModel)
    viewModelOf(::BreedsViewModel)
    viewModelOf(::SearchViewModel)
    viewModel { (breedId: Int, name: String) -> BreedDetailsViewModel(breedId, name, get(), get()) }
}