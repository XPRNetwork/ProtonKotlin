package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.*
import com.metallicus.protonsdk.repository.CurrencyBalanceRepository
import javax.inject.Inject

class CurrencyBalancesModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var currencyBalanceRepository: CurrencyBalanceRepository

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getTokenCurrencyBalance(
		hyperionHistoryUrl: String,
		accountName: String,
		tokenContract: TokenContract): Resource<TokenCurrencyBalance> {
		return try {
			val balancesResponse = currencyBalanceRepository.fetchCurrencyBalances(hyperionHistoryUrl, accountName)
			if (balancesResponse.isSuccessful) {
				val jsonObject = balancesResponse.body()
				val tokenArray = jsonObject?.getAsJsonArray("tokens")

				val tokenJsonObject = tokenArray?.find {
					tokenContract.contract == it.asJsonObject.get("contract").asString &&
					tokenContract.getSymbol() == it.asJsonObject.get("symbol").asString
				}?.asJsonObject

				val tokenContractId = tokenContract.id

				tokenJsonObject?.let { token ->
					val code = token.get("contract").asString
					val symbol = token.get("symbol").asString
					val amount = token.get("amount").asString

					val currencyBalance = CurrencyBalance(code, symbol, amount)
					currencyBalance.tokenContractId = tokenContractId
					currencyBalance.accountName = accountName

					currencyBalanceRepository.addCurrencyBalance(currencyBalance)
				}

				val tokenCurrencyBalances = currencyBalanceRepository.getTokenCurrencyBalance(tokenContractId)

				Resource.success(tokenCurrencyBalances)
			} else {
				val msg = balancesResponse.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					balancesResponse.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	suspend fun getTokenCurrencyBalances(
		hyperionHistoryUrl: String,
		accountName: String,
		tokenContractsMap: Map<String, String>): Resource<List<TokenCurrencyBalance>> {
		return try {
			val balancesResponse = currencyBalanceRepository.fetchCurrencyBalances(hyperionHistoryUrl, accountName)
			if (balancesResponse.isSuccessful) {
				val jsonObject = balancesResponse.body()
				val tokenArray = jsonObject?.getAsJsonArray("tokens")

				tokenArray?.forEach {
					val token = it.asJsonObject
					val code = token.get("contract").asString
					val symbol = token.get("symbol").asString
					val amount = token.get("amount").asString

					val tokenContractId = tokenContractsMap.getValue("$code:$symbol")

					val currencyBalance = CurrencyBalance(code, symbol, amount)
					currencyBalance.tokenContractId = tokenContractId
					currencyBalance.accountName = accountName

					currencyBalanceRepository.addCurrencyBalance(currencyBalance)
				}

				val tokenCurrencyBalances = currencyBalanceRepository.getTokenCurrencyBalances(accountName)

				Resource.success(tokenCurrencyBalances)
			} else {
				val msg = balancesResponse.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					balancesResponse.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}
}