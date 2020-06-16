package com.metallicus.protonsdk.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ChainProvider(
	@PrimaryKey
	@SerializedName("chainId") val chainId: String,

	@SerializedName("name") val name: String,
	@SerializedName("description") val description: String,
	@SerializedName("iconUrl") val iconUrl: String,
	@SerializedName("isTestnet") val isTestnet: Boolean,
	@SerializedName("chainUrl") val chainUrl: String,
	@SerializedName("hyperionHistoryUrl") val hyperionHistoryUrl: String,

	@SerializedName("explorerName") val explorerName: String,
	@SerializedName("explorerUrl") val explorerUrl: String,

	@SerializedName("resourceTokenSymbol") val resourceTokenSymbol: String,
	@SerializedName("resourceTokenContract") val resourceTokenContract: String,
	@SerializedName("systemTokenSymbol") val systemTokenSymbol: String,
	@SerializedName("systemTokenContract") val systemTokenContract: String,

	@SerializedName("createAccountUrl") val createAccountUrl: String,
	@SerializedName("updateAccountAvatarUrl") val updateAccountAvatarUrl: String,
	@SerializedName("updateAccountNameUrl") val updateAccountNameUrl: String,
	@SerializedName("exchangeRateUrl") val exchangeRateUrl: String
)