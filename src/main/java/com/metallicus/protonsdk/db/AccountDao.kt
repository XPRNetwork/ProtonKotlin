package com.metallicus.protonsdk.db

import androidx.room.*
import com.metallicus.protonsdk.model.Account
import com.metallicus.protonsdk.model.ChainAccount

/**
 * Interface for database access for [Account] related operations
 */
@Dao
interface AccountDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(account: Account)

	@Update
	suspend fun update(account: Account)

	@Transaction
	@Query("SELECT * FROM account WHERE accountName = :accountName")
	suspend fun findByAccountName(accountName: String): ChainAccount

	@Query("DELETE FROM account")
	suspend fun removeAll()
}