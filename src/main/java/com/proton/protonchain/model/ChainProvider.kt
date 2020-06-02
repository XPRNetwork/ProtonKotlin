package com.proton.protonchain.model

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
	@SerializedName("isActive") val isActive: Boolean,
	@SerializedName("isTestnet") val isTestnet: Boolean,
	@SerializedName("chainUrl") val chainUrl: String,
	@SerializedName("stateHistoryUrl") val stateHistoryUrl: String,

	@SerializedName("explorerName") val explorerName: String,
	@SerializedName("explorerAccountUrl") val explorerAccountUrl: String,
	@SerializedName("explorerKeyUrl") val explorerKeyUrl: String,
	@SerializedName("explorerTransactionUrl") val explorerTransactionUrl: String,

	@SerializedName("isAccountCreationEnabled") val isAccountCreationEnabled: Boolean,
	@SerializedName("createAccountVerifiedUrl") val createAccountVerifiedUrl: String,
	@SerializedName("requestPhoneVerificationUrl") val requestPhoneVerificationUrl: String,
	@SerializedName("verifyPhoneCodeUrl") val verifyPhoneCodeUrl: String,

	@SerializedName("accountSearchIndex") val accountSearchIndex: String,
	@SerializedName("userAvatarUpdateUrl") val userAvatarUpdateUrl: String,
	@SerializedName("userDataUpdateUrl") val userDataUpdateUrl: String,
	@SerializedName("userNameUpdateUrl") val userNameUpdateUrl: String,

	@SerializedName("resourceTokenSymbol") val resourceTokenSymbol: String,
	@SerializedName("resourceTokenContract") val resourceTokenContract: String,
	@SerializedName("systemTokenSymbol") val systemTokenSymbol: String,
	@SerializedName("systemTokenContract") val systemTokenContract: String,

	@SerializedName("usersInfoTableCode") val usersInfoTableCode: String,
	@SerializedName("usersInfoTableScope") val usersInfoTableScope: String,
	@SerializedName("tokensTableCode") val tokensTableCode: String,
	@SerializedName("tokensTableScope") val tokensTableScope: String
)