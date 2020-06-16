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
		val rate = if (currency=="USD") {
			tokenContract.rateUSD
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