package com.metallicus.protonsdk.db

import androidx.room.*
import com.metallicus.protonsdk.model.CurrencyBalance
import com.metallicus.protonsdk.model.TokenCurrencyBalance

/**
 * Interface for database access for [CurrencyBalance] related operations
 */
@Dao
interface CurrencyBalanceDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insert(currencyBalance: CurrencyBalance): Long

	@Transaction
	suspend fun updateAmount(currencyBalance: CurrencyBalance) {
		val id = insert(currencyBalance)
		if (id == -1L) {
			initializeVisible(
				currencyBalance.accountName,
				currencyBalance.code,
				currencyBalance.symbol,
				currencyBalance.visible)
			updateAmount(
				currencyBalance.accountName,
				currencyBalance.code,
				currencyBalance.symbol,
				currencyBalance.amount)
		}
	}

	@Query("UPDATE currencyBalance SET visible = :visible " +
		"WHERE accountName = :accountName AND code = :code AND symbol = :symbol AND initialized = 0")
	suspend fun initializeVisible(accountName: String, code: String, symbol: String, visible: Boolean)

	@Query("UPDATE currencyBalance SET amount = :amount, initialized = 1 " +
		"WHERE accountName = :accountName AND code = :code AND symbol = :symbol")
	suspend fun updateAmount(accountName: String, code: String, symbol: String, amount: String)

	@Transaction
	suspend fun insertOrUpdateVisible(currencyBalance: CurrencyBalance) {
		val id = insert(currencyBalance)
		if (id == -1L) {
			updateVisible(
				currencyBalance.accountName,
				currencyBalance.code,
				currencyBalance.symbol,
				currencyBalance.visible)
		}
	}

	@Query("UPDATE currencyBalance SET visible = :visible WHERE accountName = :accountName AND code = :code AND symbol = :symbol")
	suspend fun updateVisible(accountName: String, code: String, symbol: String, visible: Boolean)

	@Transaction
	@Query("SELECT * FROM currencyBalance WHERE accountName = :accountName AND code IN(:contracts) AND symbol IN(:symbols)")
	suspend fun findByAccountTokenContract(accountName: String, contracts: List<String>, symbols: List<String>): List<TokenCurrencyBalance>

	@Transaction
	@Query("SELECT * FROM currencyBalance WHERE accountName = :accountName AND code = :contract AND symbol = :symbol")
	suspend fun findByAccountTokenContract(accountName: String, contract: String, symbol: String): TokenCurrencyBalance

	@Transaction
	@Query("SELECT * FROM currencyBalance WHERE accountName = :accountName")
	suspend fun findByAccountName(accountName: String): List<TokenCurrencyBalance>

	@Query("DELETE FROM currencyBalance")
	suspend fun removeAll()
}