package com.metallicus.protonsdk.db

import androidx.room.*
import com.metallicus.protonsdk.model.TokenContract

/**
 * Interface for database access for [TokenContract] related operations
 */
@Dao
interface TokenContractDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(tokenContract: TokenContract)

	@Update
	suspend fun update(tokenContract: TokenContract)

	@Query("SELECT * FROM tokenContract WHERE id = :id")
	suspend fun findById(id: String): TokenContract

	@Query("SELECT * FROM tokenContract")
	suspend fun findAll(): List<TokenContract>

	@Query("DELETE FROM tokenContract")
	suspend fun removeAll()
}