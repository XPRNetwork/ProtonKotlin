package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName

data class AccountPermissionKey(
	@SerializedName("key") val key: String,
	@SerializedName("weight") val weight: Int
)