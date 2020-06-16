package com.metallicus.protonsdk.db

import androidx.room.*
import com.metallicus.protonsdk.model.CurrencyBalance
import com.metallicus.protonsdk.model.TokenCurrencyBalance

/**
 * Interface for database access for [CurrencyBalance] related operations
 */
@Dao
interface CurrencyBalanceDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(currencyBalance: CurrencyBalance)

	@Query("UPDATE currencyBalance SET amount = :amount WHERE accountName = :accountName AND contract = :contract AND symbol = :symbol")
	suspend fun updateAmount(accountName: String, contract: String, symbol: String, amount: String)

	@Transaction
	@Query("SELECT * FROM currencyBalance WHERE tokenContractId = :tokenContractId")
	suspend fun findByTokenContract(tokenContractId: String): TokenCurrencyBalance

	@Transaction
	@Query("SELECT * FROM currencyBalance WHERE accountName = :accountName")
	suspend fun findByAccountName(accountName: String): List<TokenCurrencyBalance>

	@Query("DELETE FROM currencyBalance")
	suspend fun removeAll()
}