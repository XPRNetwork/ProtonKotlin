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
package com.metallicus.protonsdk.model

import androidx.room.Embedded
import androidx.room.Relation
import java.text.NumberFormat
import java.util.*

data class TokenCurrencyBalance(
	@Embedded
	val currencyBalance: CurrencyBalance,

	@Relation(
		parentColumn = "tokenContractId",
		entityColumn = "id"
	)
	val tokenContract: TokenContract
) {
	private fun getBalanceDouble(selfDelegatedResources: Double): Double {
		return if (selfDelegatedResources > 0.0) {
			currencyBalance.amount.toDouble() + selfDelegatedResources
		} else {
			currencyBalance.amount.toDouble()
		}
	}

	fun formatBalance(selfDelegatedResources: Double): String {
		val balance = getBalanceDouble(selfDelegatedResources)
		val nf = NumberFormat.getNumberInstance(Locale.US)
		nf.minimumFractionDigits = tokenContract.getPrecision()
		nf.maximumFractionDigits = tokenContract.getPrecision()
		return nf.format(balance)
	}

	fun formatBalance(): String {
		return formatBalance(-1.0)
	}

	fun getBalanceForCurrencyDouble(currency: String, selfDelegatedResources: Double): Double {
		val amount = getBalanceDouble(selfDelegatedResources)
		val rate = if (tokenContract.rates.containsKey(currency)) {
			tokenContract.rates.getValue(currency)
		} else {
			0.0
		}
		return amount.times(rate)
	}

	fun getBalanceForCurrencyDouble(currency: String): Double {
		return getBalanceForCurrencyDouble(currency, -1.0)
	}

	fun formatBalanceForCurrency(currency: String, selfDelegatedResources: Double): String {
		val amountCurrency = getBalanceForCurrencyDouble(currency, selfDelegatedResources)

		val nf = NumberFormat.getCurrencyInstance(Locale.US)
		return nf.format(amountCurrency)
	}

	fun formatBalanceForCurrency(currency: String): String {
		return formatBalanceForCurrency(currency, -1.0)
	}
}