package com.proton.protonchain.db

import androidx.room.*
import com.proton.protonchain.model.Account

/**
 * Interface for database access for [Account] related operations
 */
@Dao
interface AccountDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(account: Account)

	@Update
	fun update(account: Account)

//	@Query("SELECT * FROM account " +
//		"INNER JOIN chainProvider ON account.chainId = chainProvider.id " +
//		"WHERE chainId = :chainId AND accountName = :accountName")
//	suspend fun findByAccountName(chainId: String, accountName: String): ChainAccount

	@Query("DELETE FROM account WHERE chainId = :chainId AND accountName = :accountName")
	fun remove(chainId: String, accountName: String)

	@Query("DELETE FROM account WHERE chainId = :chainId AND accountName IN(:accounts)")
	fun remove(chainId: String, accounts: List<String>)

	@Query("DELETE FROM account")
	fun removeAll()
}