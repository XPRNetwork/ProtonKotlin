package com.proton.protonchain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ChainProvider(
	@PrimaryKey
	@SerializedName("id") val id: String, // chainId

	@SerializedName("name") val name: String,
	@SerializedName("slug") val slug: String,
	@SerializedName("order") val order: Long,
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

	@SerializedName("importDescription") val importDescription: String,
	@SerializedName("isAccountCreationEnabled") val isAccountCreationEnabled: Boolean,
	@SerializedName("accountSearchIndex") val accountSearchIndex: String,
	@SerializedName("createAccountUrl") val createAccountUrl: String,
	@SerializedName("createDescription") val createDescription: String,

	@SerializedName("createAccountVerifiedUrl") val createAccountVerifiedUrl: String,
	@SerializedName("requestPhoneVerificationUrl") val requestPhoneVerificationUrl: String,
	@SerializedName("verifyPhoneCodeUrl") val verifyPhoneCodeUrl: String,

	@SerializedName("userAvatarUpdateUrl") val userAvatarUpdateUrl: String,
	@SerializedName("userDataUpdateUrl") val userDataUpdateUrl: String,
	@SerializedName("userNameUpdateUrl") val userNameUpdateUrl: String,

	@SerializedName("referralUrl") val referralUrl: String,
	@SerializedName("termsOfServiceUrl") val termsOfServiceUrl: String,

	@SerializedName("resourceTokenSymbol") val resourceTokenSymbol: String,
	@SerializedName("resourceTokenContract") val resourceTokenContract: String,
	@SerializedName("systemTokenSymbol") val systemTokenSymbol: String,
	@SerializedName("systemTokenContract") val systemTokenContract: String,

	@SerializedName("usersInfoTableCode") val usersInfoTableCode: String?,
	@SerializedName("usersInfoTableScope") val usersInfoTableScope: String?,
	@SerializedName("tokensTableCode") val tokensTableCode: String?,
	@SerializedName("tokensTableScope") val tokensTableScope: String?
) {
	constructor(data: Map<String, Any>) :
		this(
			// required params
			data["chainId"] as String,

			// optionals
			(data.getOrElse("name") { "" } as? String) ?: "",
			(data.getOrElse("slug") { "" } as? String) ?: "",
			(data.getOrElse("order") { 0 } as? Long) ?: 0,
			(data.getOrElse("description") { "" } as? String) ?: "",
			(data.getOrElse("iconUrl") { "" } as? String) ?: "",
			(data.getOrElse("isActive") { false } as? Boolean) ?: false,
			(data.getOrElse("isTestnet") { false } as? Boolean) ?: false,
			(data.getOrElse("chainUrl") { "" } as? String) ?: "",
			(data.getOrElse("stateHistoryUrl") { "" } as? String) ?: "",

			(data.getOrElse("explorerName") { "" } as? String) ?: "",
			(data.getOrElse("explorerAccountUrl") { "" } as? String) ?: "",
			(data.getOrElse("explorerKeyUrl") { "" } as? String) ?: "",
			(data.getOrElse("explorerTransactionUrl") { "" } as? String) ?: "",

			(data.getOrElse("importDescription") { "" } as? String) ?: "",
			(data.getOrElse("isAccountCreationEnabled") { false } as? Boolean) ?: false,
			(data.getOrElse("accountSearchIndex") { "" } as? String) ?: "",
			(data.getOrElse("createAccountUrl") { "" } as? String) ?: "",
			(data.getOrElse("createDescription") { "" } as? String) ?: "",

			(data.getOrElse("createAccountVerifiedUrl") { "" } as? String) ?: "",
			(data.getOrElse("requestPhoneVerificationUrl") { "" } as? String) ?: "",
			(data.getOrElse("verifyPhoneCodeUrl") { "" } as? String) ?: "",

			(data.getOrElse("userAvatarUpdateUrl") { "" } as? String) ?: "",
			(data.getOrElse("userDataUpdateUrl") { "" } as? String) ?: "",
			(data.getOrElse("userNameUpdateUrl") { "" } as? String) ?: "",

			(data.getOrElse("referralUrl") { "" } as? String) ?: "",
			(data.getOrElse("termsOfServiceUrl") { "" } as? String) ?: "",

			(data.getOrElse("resourceTokenSymbol") { "" } as? String) ?: "",
			(data.getOrElse("resourceTokenContract") { "" } as? String) ?: "",
			(data.getOrElse("systemTokenSymbol") { "" } as? String) ?: "",
			(data.getOrElse("systemTokenContract") { "" } as? String) ?: "",

			(data.getOrElse("usersInfoTableCode") { "" } as? String) ?: "",
			(data.getOrElse("usersInfoTableScope") { "" } as? String) ?: "",
			(data.getOrElse("tokensTableCode") { "" } as? String) ?: "",
			(data.getOrElse("tokensTableScope") { "" } as? String) ?: ""
		)
}