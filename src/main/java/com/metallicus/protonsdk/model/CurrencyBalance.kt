package com.metallicus.protonsdk.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters

@Entity(
	indices = [(Index("tokenContractId", "accountName"))],
	primaryKeys = ["tokenContractId", "accountName"])
@TypeConverters(DefaultTypeConverters::class)
data class CurrencyBalance(
	@SerializedName("code") val code: String,
	@SerializedName("symbol") val symbol: String,
	@SerializedName("amount") val amount: String,
	@SerializedName("visible") var visible: Boolean,
	@SerializedName("initialized") var initialized: Boolean = false
) {
	lateinit var tokenContractId: String
	lateinit var accountName: String
}