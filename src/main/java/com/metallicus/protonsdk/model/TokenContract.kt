package com.metallicus.protonsdk.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters

@Entity
@TypeConverters(DefaultTypeConverters::class)
data class TokenContract(
	@PrimaryKey
	@SerializedName("id") val id: String,

	@SerializedName("tcontract") val contract: String,
	@SerializedName("tname") val name: String,
	@SerializedName("url") val url: String,
	@SerializedName("desc") val description: String,
	@SerializedName("iconurl") val iconUrl: String,
	@SerializedName("symbol") val precisionSymbol: String,
	@SerializedName("blisted") val blacklisted: Int
) {
	var rateUSD: Double = 0.0

//	lateinit var supply: String
//	lateinit var maxString: String
//	lateinit var issuer: String

	fun getSymbol(): String {
		return precisionSymbol.split(",")[1]
	}

	fun getPrecision(): Int {
		val precision = precisionSymbol.split(",")[0]
		return precision.toInt()
	}

//	fun formatRate(currency: String): String {
//		val value = if (rates.containsKey(currency)) { rates.getValue(currency) } else { 0.0 }
//		val cf = NumberFormat.getCurrencyInstance(Locale.US)
//		cf.minimumFractionDigits = 2
//		cf.maximumFractionDigits = 6
//		return cf.format(value)
//	}
//
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