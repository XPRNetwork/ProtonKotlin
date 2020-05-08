package com.proton.protonchain.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.proton.protonchain.model.TokenContract

/**
 * Interface for database access for [TokenContract] related operations
 */
@Dao
interface TokenContractDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(tokenContract: TokenContract)

	@Update
	fun update(tokenContract: TokenContract)

	@Query("SELECT * FROM tokenContract WHERE id = :id")
	fun findById(id: String): LiveData<TokenContract>

	@Query("SELECT * FROM tokenContract WHERE chainId = :chainId")
	suspend fun findAllByChainId(chainId: String): List<TokenContract>

	@Query("DELETE FROM tokenContract WHERE chainId = :chainId")
	fun removeAllByChainId(chainId: String)

	@Query("DELETE FROM tokenContract")
	fun removeAll()
}