/*
 * Copyright (c) 2020 Proton Chain LLC, Delaware
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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