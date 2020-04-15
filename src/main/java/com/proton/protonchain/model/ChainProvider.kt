package com.proton.protonchain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ChainProvider(
	@PrimaryKey
	@SerializedName("id") val id: String,

	@SerializedName("name") val name: String,
	@SerializedName("description") val description: String,
	@SerializedName("importDescription") val importDescription: String,
	@SerializedName("createDescription") val createDescription: String,
	@SerializedName("isActive") val isActive: Boolean,
	@SerializedName("isTestnet") val isTestnet: Boolean,
	@SerializedName("order") val order: Long,
	@SerializedName("iconUrl") val iconUrl: String,
	@SerializedName("createAccountUrl") val createAccountUrl: String,

	@SerializedName("supportsGetCurrencyBalances") val supportsGetCurrencyBalances: Boolean,
	@SerializedName("supportsPaidAccountCreation") val supportsPaidAccountCreation: Boolean,
	@SerializedName("isAccountCreationEnabled") val isAccountCreationEnabled: Boolean,
	@SerializedName("supportsStateHistory") val supportsStateHistory: Boolean,

	@SerializedName("defaultChainUrl") val defaultChainUrl: String,
	@SerializedName("defaultExplorerName") val defaultExplorerName: String,
	@SerializedName("defaultExplorerKeyUrl") val defaultExplorerKeyUrl: String,
	@SerializedName("defaultExplorerAccountUrl") val defaultExplorerAccountUrl: String,
	@SerializedName("defaultExplorerTransactionUrl") val defaultExplorerTransactionUrl: String,
	@SerializedName("defaultStateHistoryUrl") val defaultStateHistoryUrl: String,

	// Lynx Chain
	@SerializedName("isLynxChain") val isLynxChain: Boolean,
	@SerializedName("lynxChainRequestPhoneVerificationUrl") val lynxChainRequestPhoneVerificationUrl: String,
	@SerializedName("lynxChainVerifyPhoneCodeUrl") val lynxChainVerifyPhoneCodeUrl: String,
	@SerializedName("lynxChainCreateAccountVerifiedUrl") val lynxChainCreateAccountVerifiedUrl: String,
	@SerializedName("lynxChainUserNameUpdateUrl") val lynxChainUserNameUpdateUrl: String,
	@SerializedName("lynxChainUserAvatarUpdateUrl") val lynxChainUserAvatarUpdateUrl: String,
	@SerializedName("lynxChainUserDataUpdateUrl") val lynxChainUserDataUpdateUrl: String,
	@SerializedName("lynxChainAccountSearchIndex") val lynxChainAccountSearchIndex: String,
	@SerializedName("usersInfoTableScope") val usersInfoTableScope: String?,
	@SerializedName("usersInfoTableCode") val usersInfoTableCode: String?
) {
	constructor(data: Map<String, Any>) :
		this(
			// required params
			data["chainId"] as String,

			// optionals
			(data.getOrElse("name") { "" } as? String) ?: "",
			(data.getOrElse("description") { "" } as? String) ?: "",
			(data.getOrElse("importDescription") { "" } as? String) ?: "",
			(data.getOrElse("createDescription") { "" } as? String) ?: "",
			(data.getOrElse("isActive") { false } as? Boolean) ?: false,
			(data.getOrElse("isTestnet") { false } as? Boolean) ?: false,
			(data.getOrElse("order") { 0 } as? Long) ?: 0,
			(data.getOrElse("iconUrl") { "" } as? String) ?: "",
			(data.getOrElse("createAccountUrl") { "" } as? String) ?: "",

			(data.getOrElse("supportsGetCurrencyBalances") { false } as? Boolean) ?: false,
			(data.getOrElse("supportsPaidAccountCreation") { false } as? Boolean) ?: false,
			(data.getOrElse("isAccountCreationEnabled") { false } as? Boolean) ?: false,
			(data.getOrElse("supportsStateHistory") { false } as? Boolean) ?: false,

			(data.getOrElse("defaultChainUrl") { "" } as? String) ?: "",
			(data.getOrElse("defaultExplorerName") { "" } as? String) ?: "",
			(data.getOrElse("defaultExplorerKeyUrl") { "" } as? String) ?: "",
			(data.getOrElse("defaultExplorerAccountUrl") { "" } as? String) ?: "",
			(data.getOrElse("defaultExplorerTransactionUrl") { "" } as? String) ?: "",
			(data.getOrElse("defaultStateHistoryUrl") { "" } as? String) ?: "",

			(data.getOrElse("isLynxChain") { false } as? Boolean) ?: false,
			(data.getOrElse("lynxChainRequestPhoneVerificationUrl") { "" } as? String) ?: "",
			(data.getOrElse("lynxChainVerifyPhoneCodeUrl") { "" } as? String) ?: "",
			(data.getOrElse("lynxChainCreateAccountVerifiedUrl") { "" } as? String) ?: "",
			(data.getOrElse("lynxChainUserNameUpdateUrl") { "" } as? String) ?: "",
			(data.getOrElse("lynxChainUserAvatarUpdateUrl") { "" } as? String) ?: "",
			(data.getOrElse("lynxChainUserDataUpdateUrl") { "" } as? String) ?: "",
			(data.getOrElse("lynxChainAccountSearchIndex") { "" } as? String) ?: "",
			(data.getOrElse("usersInfoTableScope") { "" } as? String) ?: "",
			(data.getOrElse("usersInfoTableCode") { "" } as? String) ?: ""
		)
}