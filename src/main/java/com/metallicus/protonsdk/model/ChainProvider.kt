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
	@SerializedName("stateHistoryUrl") val stateHistoryUrl: String,

	@SerializedName("explorerName") val explorerName: String,
	@SerializedName("explorerUrl") val explorerUrl: String,

	@SerializedName("resourceTokenSymbol") val resourceTokenSymbol: String,
	@SerializedName("resourceTokenContract") val resourceTokenContract: String,
	@SerializedName("systemTokenSymbol") val systemTokenSymbol: String,
	@SerializedName("systemTokenContract") val systemTokenContract: String,

	@SerializedName("createAccountApi") val createAccountApi: String,
	@SerializedName("requestEmailApi") val requestEmailApi: String,
	@SerializedName("verifyEmailApi") val verifyEmailApi: String,

	@SerializedName("updateChainAccountAvatar") val updateChainAccountAvatar: String,
	@SerializedName("updateChainAccountName") val updateChainAccountName: String
)