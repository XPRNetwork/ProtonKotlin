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

import com.google.gson.annotations.SerializedName

data class ActionTraceActData(
	@SerializedName("from") val from: String? = "",
	@SerializedName("to") val to: String? = "",
	@SerializedName("quantity") val quantity: String? = "",
	@SerializedName("memo") val memo: String? = "",
	@SerializedName("receiver") val receiver: String? = "",
	@SerializedName("stake_cpu_quantity") val stakeCpuQuantity: String? = "0.0",
	@SerializedName("stake_net_quantity") val stakeNetQuantity: String? = "0.0",
	@SerializedName("unstake_cpu_quantity") val unStakeCpuQuantity: String? = "0.0",
	@SerializedName("unstake_net_quantity") val unStakeNetQuantity: String? = "0.0"
) {
	fun quantityToDouble(): Double {
		return quantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun stakeCpuQuantityToDouble(): Double {
		return stakeCpuQuantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun stakeNetQuantityToDouble(): Double {
		return stakeNetQuantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun stakeTotal(): String {
		val cpu = stakeCpuQuantityToDouble()
		val net = stakeNetQuantityToDouble()
		return (cpu.plus(net)).toString()
	}

	fun unStakeCpuQuantityToDouble(): Double {
		return unStakeCpuQuantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun unStakeNetQuantityToDouble(): Double {
		return unStakeNetQuantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun unStakeTotal(): String {
		val cpu = unStakeCpuQuantityToDouble()
		val net = unStakeNetQuantityToDouble()
		return (cpu.plus(net)).toString()
	}
}