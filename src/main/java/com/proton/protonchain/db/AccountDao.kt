package com.proton.protonchain.db

import androidx.room.*
import com.proton.protonchain.model.Account
import com.proton.protonchain.model.ChainAccount

/**
 * Interface for database access for [Account] related operations
 */
@Dao
interface AccountDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(account: Account)

	@Update
	fun update(account: Account)

	@Transaction
	@Query("SELECT * FROM account WHERE accountChainId = :chainId AND accountName = :accountName")
	suspend fun findByAccountName(chainId: String, accountName: String): ChainAccount

	@Query("DELETE FROM account WHERE accountChainId = :chainId AND accountName = :accountName")
	fun remove(chainId: String, accountName: String)

	@Query("DELETE FROM account WHERE accountChainId = :chainId AND accountName IN(:accounts)")
	fun remove(chainId: String, accounts: List<String>)

	@Query("DELETE FROM account")
	fun removeAll()
}