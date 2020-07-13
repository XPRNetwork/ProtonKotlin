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
import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters
import com.metallicus.protonsdk.db.EOSTypeConverters
import com.metallicus.protonsdk.db.ProtonTypeConverters
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
@TypeConverters(DefaultTypeConverters::class, EOSTypeConverters::class, ProtonTypeConverters::class)
data class Action(
	@SerializedName("global_action_seq") val globalActionSeq: Int,
	@SerializedName("block_num") val blockNum: Int,
	@SerializedName("block_time") val blockTime: String,
	@SerializedName("action_trace")
	@Embedded(prefix = "action_trace_") val actionTrace: ActionTrace
) {
	lateinit var accountName: String

	lateinit var accountContact: AccountContact

	enum class IconType { AVATAR, SEND, RECEIVE, STAKE, UNSTAKE, BUY_RAM }

	fun isTransfer(): Boolean {
		return (actionTrace.act.name == "transfer")
	}

	fun isSender(): Boolean {
		return (accountName == actionTrace.act.data?.from && actionTrace.act.data.from != actionTrace.act.data.to)
	}

	fun getIconType(): IconType {
		return IconType.AVATAR

//		return if (accountContact.isLynxChain) {
//			IconType.AVATAR
//		} else {
//			if (actionTrace.act.data?.to == "eosio.ramfee" || actionTrace.act.data?.to == "eosio.ram") {
//				IconType.BUY_RAM
//			} else if (actionTrace.act.data?.to == "eosio.stake") {
//				IconType.STAKE
//			} else if (actionTrace.act.data?.from == "eosio.stake") {
//				IconType.UNSTAKE
//			} else if (!isSender()) {
//				IconType.RECEIVE
//			} else {
//				IconType.SEND
//			}
//		}
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