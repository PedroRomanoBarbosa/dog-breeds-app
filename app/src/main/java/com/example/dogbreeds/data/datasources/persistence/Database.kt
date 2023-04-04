package com.example.dogbreeds.data.datasources.persistence

import androidx.room.*

const val DATABASE_V1 = 1
const val BREED_TABLE = "breeds"

@Entity(tableName = BREED_TABLE)
data class BreedLocal(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String? = null,
    val category: String? = null,
    val origin: String? = null,
    val temperament: String? = null,
    // Pagination
    val page: Int,
    val total: Int,
)

@Dao
interface BreedsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breeds: List<BreedLocal>)

    @Query("SELECT * FROM $BREED_TABLE WHERE page = :page ORDER BY name ASC")
    fun getBreedsByPage(page: Int): List<BreedLocal>

    @Query("SELECT * FROM $BREED_TABLE WHERE id = :breedId")
    fun getBreedById(breedId: Int): BreedLocal

    @Query("SELECT * FROM $BREED_TABLE WHERE name LIKE '%' || :term || '%'")
    fun searchBreedsByName(term: String): List<BreedLocal>

    @Query("DELETE FROM $BREED_TABLE")
    suspend fun clearAll()
}

@Database(version = DATABASE_V1, entities = [BreedLocal::class])
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "Database"
    }

    abstract fun breedsDao(): BreedsDao
}