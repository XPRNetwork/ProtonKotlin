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

	@Query("SELECT * FROM tokenContract")
	suspend fun findAll(): List<TokenContract>

	@Query("UPDATE tokenContract SET rates = :rates WHERE id = :tokenContractId")
	suspend fun updateRates(tokenContractId: String, rates: String)

	@Query("DELETE FROM tokenContract")
	suspend fun removeAll()
}