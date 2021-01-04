package com.metallicus.protonsdk.db

import androidx.room.*
import com.metallicus.protonsdk.model.ESRSession

/**
 * Interface for database access for [ESRSession] related operations
 */
@Dao
interface ESRSessionDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(esrSession: ESRSession)

	@Update
	suspend fun update(esrSession: ESRSession)

	@Query("SELECT * FROM esrSession WHERE id = :id")
	suspend fun findById(id: String): ESRSession

	@Query("SELECT * FROM esrSession")
	suspend fun findAll(): List<ESRSession>

	@Query("DELETE FROM esrSession")
	suspend fun removeAll()
}