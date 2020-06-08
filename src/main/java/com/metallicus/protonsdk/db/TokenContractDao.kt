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

	@Query("SELECT * FROM tokenContract WHERE chainId = :chainId")
	suspend fun findAllByChainId(chainId: String): List<TokenContract>

	@Query("DELETE FROM tokenContract WHERE chainId = :chainId")
	suspend fun removeAllByChainId(chainId: String)

	@Query("DELETE FROM tokenContract")
	suspend fun removeAll()
}