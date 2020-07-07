package com.metallicus.protonsdk.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.metallicus.protonsdk.model.ChainProvider

/**
 * Interface for database access for [ChainProvider] related operations
 */
@Dao
interface ChainProviderDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(chainProvider: ChainProvider)

	@Query("SELECT * FROM chainProvider WHERE chainId = :id")
	suspend fun findById(id: String): ChainProvider

	@Query("SELECT * FROM chainProvider")
	suspend fun findAll(): List<ChainProvider>

	@Query("DELETE FROM chainProvider")
	suspend fun removeAll()
}