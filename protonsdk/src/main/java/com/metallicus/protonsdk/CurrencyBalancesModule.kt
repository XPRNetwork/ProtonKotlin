/*
 * Copyright (c) 2021 Proton Chain LLC, Delaware
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
package com.metallicus.protonsdk

import android.content.Context
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.*
import com.metallicus.protonsdk.repository.CurrencyBalanceRepository
import com.metallicus.protonsdk.repository.TokenContractRepository
import javax.inject.Inject

/**
 * Helper class used for [CurrencyBalance] based operations
 */
class CurrencyBalancesModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var tokenContractRepository: TokenContractRepository

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

				val currencyBalance = CurrencyBalance(tokenContract.contract, tokenContract.getSymbol())

				tokenJsonObject?.let { token ->
					val amount = token.get("amount").asString

					currencyBalance.amount = amount
				}

				val tokenCurrencyBalance = TokenCurrencyBalance(tokenContract, currencyBalance)

				Resource.success(tokenCurrencyBalance)
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

	private fun isValidEmptyToken(contract: String): Boolean {
		return (contract == "eosio.token" || contract == "xtokens")
	}

	suspend fun getTokenCurrencyBalances(
		hyperionHistoryUrl: String,
		accountName: String,
		tokenContractsMap: Map<String, TokenContract>,
		addEmptyTokens: Boolean = false
	): Resource<List<TokenCurrencyBalance>> {
		return try {
			val balancesResponse = currencyBalanceRepository.fetchCurrencyBalances(hyperionHistoryUrl, accountName)
			if (balancesResponse.isSuccessful) {
				val tokenCurrencyBalances = mutableListOf<TokenCurrencyBalance>()

				balancesResponse.body()?.getAsJsonArray("tokens")?.let { tokensArray ->
					val currencyBalancesMap = tokensArray.associateBy {
						val token = it.asJsonObject
						val contract = token.get("contract").asString
						val symbol = token.get("symbol").asString
						"$contract:$symbol"
					}

					tokenContractsMap.forEach { tokenContractMapEntry ->
						val tokenContract = tokenContractMapEntry.value
						val contract = tokenContract.contract
						val symbol = tokenContract.getSymbol()

						val currencyBalanceKey = "$contract:$symbol"

						val currencyBalance = CurrencyBalance(contract, symbol)

						if (currencyBalancesMap.containsKey(currencyBalanceKey)) {
							val token = currencyBalancesMap[currencyBalanceKey]?.asJsonObject
							currencyBalance.amount = token?.get("amount")?.asString.orEmpty()

							val tokenCurrencyBalance = TokenCurrencyBalance(tokenContract, currencyBalance)
							tokenCurrencyBalances.add(tokenCurrencyBalance)
						} else if (addEmptyTokens && isValidEmptyToken(contract)) { // only add valid empty tokens
							val tokenCurrencyBalance = TokenCurrencyBalance(tokenContract, currencyBalance)
							tokenCurrencyBalances.add(tokenCurrencyBalance)
						}
					}

					// add custom tokens
					/*currencyBalancesMap.forEach { currencyBalancesMapEntry ->
						val token = currencyBalancesMapEntry.value.asJsonObject
						val contract = token.get("contract").asString
						val symbol = token.get("symbol").asString

						val currencyBalanceKey = "$contract:$symbol"

						if (!tokenContractsMap.containsKey(currencyBalanceKey)) {
							val amount = token.get("amount").asString
							val precision = token.get("precision").asInt
							val precisionSymbol = "$precision,$symbol"
							val tokenContract = TokenContract(
								id = currencyBalanceKey,
								contract = contract,
								name = symbol,
								url = "",
								description = "",
								iconUrl = "",
								precisionSymbol = precisionSymbol,
								blacklisted = 0)
							tokenContract.rates = mapOf(Pair("USD", TokenContractRate()))

							tokenContractRepository.addTokenContract(tokenContract)

							val currencyBalance = CurrencyBalance(contract, symbol, amount)

							val tokenCurrencyBalance = TokenCurrencyBalance(tokenContract, currencyBalance)
							tokenCurrencyBalances.add(tokenCurrencyBalance)
						}
					}*/
				}

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