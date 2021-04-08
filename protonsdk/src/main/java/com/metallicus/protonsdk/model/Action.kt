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

import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.*
import kotlin.math.sign

@Entity(
	indices = [(Index(
		"accountName",
		"action_trace_act_account",
		"action_trace_act_data_quantity"
	))],
	primaryKeys = ["accountName", "action_trace_trxId", "action_trace_act_name", "action_trace_act_authorization"]
)
data class Action(
	@SerializedName("global_action_seq") val globalActionSeq: Int,
	@SerializedName("block_num") val blockNum: Int,
	@SerializedName("block_time") val blockTime: String,
	@SerializedName("action_trace")
	@Embedded(prefix = "action_trace_") val actionTrace: ActionTrace
) {
	@NonNull
	lateinit var accountName: String

	lateinit var accountContact: AccountContact

	fun isTransfer(): Boolean {
		return (actionTrace.act.name == "transfer")
	}

	fun isSender(): Boolean {
		return (accountName == actionTrace.act.data?.from && actionTrace.act.data.from != actionTrace.act.data.to)
	}

	fun getDisplayName(): String {
		return when {
			actionTrace.act.data?.to == "eosio.ramfee" -> "Buy RAM Fee"
			actionTrace.act.data?.to == "eosio.ram" -> "Buy RAM"
			actionTrace.act.data?.to == "eosio.stake" -> "Staked Resources"
			actionTrace.act.data?.from == "eosio.stake" -> "Unstaked Resources"
			else -> accountContact.getDisplayName()
		}
	}

	private fun getAmount(): Double {
		var amount = 0.0
		actionTrace.act.data?.let { data ->
			amount = data.quantityToDouble()
			if (isSender()) {
				amount = -amount
			}
		}
		return amount
	}

	fun getAmountStr(precision: Long): String {
		val amount = getAmount()

		val nf = NumberFormat.getNumberInstance(Locale.US)
		nf.minimumFractionDigits = precision.toInt()
		nf.maximumFractionDigits = precision.toInt()

		var amountStr = nf.format(amount)

		if (amount.sign != -1.0) {
			amountStr = "+$amountStr"
		}

		return amountStr
	}

	fun getAmountCurrency(rate: Double): String {
		val amount = getAmount()

		val nf = NumberFormat.getCurrencyInstance(Locale.US)

		val amountCurrency = amount.times(rate)

		var amountCurrencyStr = nf.format(amountCurrency)
		if (amount.sign != -1.0) {
			amountCurrencyStr = "+$amountCurrencyStr"
		}

		return amountCurrencyStr
	}
}

data class ActionTrace(
	@SerializedName("trx_id") val trxId: String,
	@SerializedName("act")
	@Embedded(prefix = "act_") val act: ActionTraceAct
)

data class ActionTraceAct(
	@SerializedName("account") val account: String,
	@SerializedName("name") val name: String,
	@SerializedName("authorization") val authorization: List<ActionTraceActAuthorization>,
	@SerializedName("data")
	@Embedded(prefix = "data_") val data: ActionTraceActData?
)

data class ActionTraceActAuthorization(
	@SerializedName("actor") val actor: String,
	@SerializedName("permission") val permission: String
)

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