package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName

data class AccountPermissionRequiredAuth(
	@SerializedName("threshold") val threshold: Int,
	@SerializedName("keys") val keys: List<AccountPermissionKey>
)