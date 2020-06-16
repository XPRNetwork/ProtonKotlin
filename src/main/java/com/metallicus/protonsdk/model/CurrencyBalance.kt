package com.metallicus.protonsdk.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters

@Entity(
	indices = [(Index("tokenContractId", "accountName"))],
	primaryKeys = ["tokenContractId", "accountName"])
@TypeConverters(DefaultTypeConverters::class)
data class CurrencyBalance(
	@SerializedName("contract") val contract: String,
	@SerializedName("symbol") val symbol: String,
	@SerializedName("amount") val amount: String
) {
	lateinit var tokenContractId: String
	lateinit var accountName: String
}