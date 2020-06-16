package com.metallicus.protonsdk.db

import androidx.room.*
import com.metallicus.protonsdk.model.AccountContact

/**
 * Interface for database access for [AccountContact] related operations
 */
@Dao
interface AccountContactDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun insert(accountContact: AccountContact)

	@Update
	fun update(accountContact: AccountContact)

	@Query("SELECT * FROM accountContact WHERE accountName = :accountName")
	suspend fun findByAccountName(accountName: String): List<AccountContact>

	@Query("DELETE FROM accountContact")
	fun removeAll()
}