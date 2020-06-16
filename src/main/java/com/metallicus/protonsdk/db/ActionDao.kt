package com.metallicus.protonsdk.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.metallicus.protonsdk.model.Action

/**
 * Interface for database access for [Action] related operations
 */
@Dao
interface ActionDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(action: Action)

	@Query("SELECT * FROM `action` " +
		"WHERE accountName = :accountName " +
		"AND ((action_trace_act_account = :contract AND action_trace_act_data_quantity LIKE '% ' || :symbol) OR action_trace_act_account = 'eosio')")
	suspend fun findBySystemTokenContract(accountName: String, contract: String, symbol: String): List<Action>

	@Query("SELECT * FROM `action` " +
		"WHERE accountName = :accountName " +
		"AND action_trace_act_account = :contract " +
		"AND action_trace_act_data_quantity LIKE '% ' || :symbol")
	suspend fun findByTokenContract(accountName: String, contract: String, symbol: String): List<Action>

	@Query("DELETE FROM `action`")
	suspend fun removeAll()
}