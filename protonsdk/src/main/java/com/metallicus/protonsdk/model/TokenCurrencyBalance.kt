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
	fun isSystemToken(): Boolean {
		return (tokenContract.isSystemToken)
	}

	private fun getBalanceDouble(adjustments: Double = 0.0): Double {
		return currencyBalance.getAmountDouble() + adjustments
	}

	fun formatBalance(adjustments: Double = 0.0): String {
		val balance = getBalanceDouble(adjustments)
		val nf = NumberFormat.getNumberInstance(Locale.US)
		nf.minimumFractionDigits = tokenContract.getPrecision()
		nf.maximumFractionDigits = tokenContract.getPrecision()
		return nf.format(balance)
	}

	fun getBalanceForCurrencyDouble(currency: String, adjustments: Double = 0.0): Double {
		val amount = getBalanceDouble(adjustments)
		val rate = tokenContract.getRate(currency)
		return amount.times(rate)
	}

	fun formatBalanceForCurrency(currency: String, adjustments: Double = 0.0): String {
		val amountCurrency = getBalanceForCurrencyDouble(currency, adjustments)

		val nf = NumberFormat.getCurrencyInstance(Locale.US)
		return nf.format(amountCurrency)
	}
}