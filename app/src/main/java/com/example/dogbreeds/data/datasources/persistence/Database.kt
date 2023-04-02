package com.example.dogbreeds.data.datasources.persistence

import androidx.room.*

const val BREED_TABLE = "breeds"
const val BREED_REMOTE_KEYS_TABLE = "breedRemoteKeys"

@Entity(tableName = BREED_TABLE)
data class BreedLocal(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val page: Int,
    val total: Int,
)

@Entity(tableName = BREED_REMOTE_KEYS_TABLE)
data class BreedRemoteKeys(
    @PrimaryKey
    val breedId: Long,
    val prevKey: Int?,
    val nextKey: Int?
)

@Dao
interface BreedsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breeds: List<BreedLocal>)

    /*
    @Query("SELECT * FROM $BREED_TABLE ORDER BY name ASC")
    fun pagingSource(): PagingSource<Int, Breed>
     */

    @Query("SELECT * FROM $BREED_TABLE WHERE page = :page ORDER BY name ASC")
    fun getBreedsByPage(page: Int): List<BreedLocal>

    @Query("DELETE FROM $BREED_TABLE")
    suspend fun clearAll()
}

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(remoteKey: List<BreedRemoteKeys>)

    @Query("SELECT * FROM $BREED_REMOTE_KEYS_TABLE WHERE breedId = :breedId")
    fun remoteKeysByBreedId(breedId: Long): BreedRemoteKeys?

    @Query("DELETE FROM $BREED_REMOTE_KEYS_TABLE")
    fun clearRemoteKeys()
}

@Database(version = 1, entities = [BreedLocal::class, BreedRemoteKeys::class])
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "Database"
    }

    abstract fun breedsDao(): BreedsDao

    abstract fun remoteKeysDao(): RemoteKeysDao
}