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

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters
import java.text.NumberFormat
import java.util.*

@Entity
@TypeConverters(DefaultTypeConverters::class)
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
	var isSystemToken: Boolean = false
) {
	lateinit var rates: Map<String, Double>

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

	fun formatRate(currency: String): String {
		val value = if (rates.containsKey(currency)) { rates.getValue(currency) } else { 0.0 }
		val cf = NumberFormat.getCurrencyInstance(Locale.US)
		cf.minimumFractionDigits = 2
		cf.maximumFractionDigits = 6
		return cf.format(value)
	}

//	fun formatSupply(includeSymbol: Boolean): String {
//		return if (supply.isEmpty()) {
//			""
//		} else {
//			val supplyDouble = supply.substringBefore(" ").toDouble()
//
//			val nf = NumberFormat.getNumberInstance(Locale.US)
//			nf.minimumFractionDigits = precision.toInt()
//			nf.maximumFractionDigits = precision.toInt()
//			nf.format(supplyDouble) + if (includeSymbol) " $contractSymbol" else ""
//		}
//	}
//
//	fun formatMaxSupply(includeSymbol: Boolean): String {
//		return if (maxSupply.isEmpty()) {
//			""
//		} else {
//			val maxSupplyDouble = maxSupply.substringBefore(" ").toDouble()
//
//			val nf = NumberFormat.getNumberInstance(Locale.US)
//			nf.minimumFractionDigits = precision.toInt()
//			nf.maximumFractionDigits = precision.toInt()
//			nf.format(maxSupplyDouble) + if (includeSymbol) " $contractSymbol" else ""
//		}
//	}
}