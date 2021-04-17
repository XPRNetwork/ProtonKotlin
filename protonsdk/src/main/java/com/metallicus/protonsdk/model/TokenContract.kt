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

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.*

@Entity
data class TokenContract(
	@PrimaryKey
	@SerializedName("id") var id: String,

	@SerializedName("tcontract") val contract: String,
	@SerializedName("tname") val name: String,
	@SerializedName("url") val url: String,
	@SerializedName("desc") val description: String,
	@SerializedName("iconurl") val iconUrl: String,
	@SerializedName("symbol") val precisionSymbol: String,
	@SerializedName("blisted") val blacklisted: Int,
	var isSystemToken: Boolean = false,
	var rank: Int = 0,
	var rates: Map<String, TokenContractRate> = mapOf(Pair("USD", TokenContractRate())),
) {
//	lateinit var supply: String
//	lateinit var maxSupply: String
//	lateinit var issuer: String

	fun getSymbol(): String {
		return precisionSymbol.split(",")[1]
	}

	fun getPrecision(): Int {
		val precision = precisionSymbol.split(",")[0]
		return precision.toInt()
	}

	fun getRate(currency: String): Double {
		return if (rates.contains(currency)) {
			rates[currency]?.price ?: 0.0
		} else {
			rates["USD"]?.price ?: 0.0
		}
	}

	fun formatRateForCurrency(currency: String): String {
		val rate = getRate(currency)
		val nf = NumberFormat.getCurrencyInstance(Locale.US)
		return nf.format(rate)
	}

	fun formatAmountAsAsset(amount: Double): String {
		val amountBD = amount.toBigDecimal()

		val symbol = getSymbol()
		val precision = getPrecision()

		val nf = NumberFormat.getNumberInstance(Locale.US)
		nf.minimumFractionDigits = precision
		nf.maximumFractionDigits = precision

		return "${nf.format(amountBD)} $symbol"
	}

	fun formatAmountForCurrency(amount: Double, currency: String): String {
		val rate = getRate(currency)
		val amountCurrency = amount.times(rate)

		val nf = NumberFormat.getCurrencyInstance(Locale.US)
		return nf.format(amountCurrency)
	}

	fun getPriceChangePercent(currency: String): Double {
		return if (rates.contains(currency)) {
			rates[currency]?.priceChangePercent ?: 0.0
		} else {
			rates["USD"]?.priceChangePercent ?: 0.0
		}
	}

	fun formatPriceChangePercent(currency: String): String {
		val priceChangePercent = getPriceChangePercent(currency)
		return if (priceChangePercent <= 0.0) {
			"$priceChangePercent%"
		} else {
			"+$priceChangePercent%"
		}
	}

	fun getMarketCap(currency: String): Double {
		return if (rates.contains(currency)) {
			rates[currency]?.marketCap ?: 0.0
		} else {
			rates["USD"]?.marketCap ?: 0.0
		}
	}
}