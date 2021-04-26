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

import com.google.gson.annotations.SerializedName

data class SwapPool(
	@SerializedName("lt_symbol") val symbol: String,
	@SerializedName("creator") val creator: String,
	@SerializedName("memo") val memo: String,
	@SerializedName("pool1") val pool1: SwapPoolAsset,
	@SerializedName("pool2") val pool2: SwapPoolAsset,
	@SerializedName("hash") val hash: String,
	@SerializedName("fee") val fee: SwapPoolFee,
	@SerializedName("active") val active: Int,
	@SerializedName("reserved") val reserved: Int
) {
	fun getPool1Symbol(): String {
		return memo.split("<>")[0]
	}

	fun getPool2Symbol(): String {
		return memo.split("<>")[1]
	}

	fun getPool1Amount(): Double {
		return pool1.quantity.substringBefore(" ").toDouble()
	}

	fun getPool2Amount(): Double {
		return pool2.quantity.substringBefore(" ").toDouble()
	}

	fun getPool1Rate(): Double {
		return getPool1Amount() / getPool2Amount()
	}

	fun getPool1Contract(): String {
		return pool1.contract
	}
}

data class SwapPoolAsset(
	@SerializedName("quantity") val quantity: String,
	@SerializedName("contract") val contract: String
)

data class SwapPoolFee(
	@SerializedName("exchange_fee") val exchangeFee: Long,
	@SerializedName("add_liquidity_fee") val addLiquidityFee: Long,
	@SerializedName("remove_liquidity_fee") val removeLiquidityFee: Long
)

data class SwapPoolMapEntry(
	val rate: Double,
	val tokenCurrencyBalance: TokenCurrencyBalance
)