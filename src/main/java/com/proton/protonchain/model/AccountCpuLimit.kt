package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName

data class AccountCpuLimit(
	@SerializedName("used") val used: Long,
	@SerializedName("available") val available: Long,
	@SerializedName("max") val max: Long
)