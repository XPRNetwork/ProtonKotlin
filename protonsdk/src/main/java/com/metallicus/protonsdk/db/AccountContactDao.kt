package com.metallicus.protonsdk.db

import androidx.room.*
import com.metallicus.protonsdk.model.AccountContact

/**
 * Interface for database access for [AccountContact] related operations
 */
@Dao
interface AccountContactDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insert(accountContact: AccountContact)

	@Update
	suspend fun update(accountContact: AccountContact)

	@Query("SELECT * FROM accountContact WHERE accountName = :accountName")
	suspend fun findByAccountName(accountName: String): List<AccountContact>

	@Query("DELETE FROM accountContact")
	suspend fun removeAll()
}