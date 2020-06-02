package com.metallicus.protonsdk.model

import com.google.gson.annotations.SerializedName

data class AccountPermission(
	@SerializedName("perm_name") val permName: String,
	@SerializedName("parent") val parent: String,
	@SerializedName("required_auth") val requiredAuth: AccountPermissionRequiredAuth
)