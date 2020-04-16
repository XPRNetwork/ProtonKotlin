package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName

data class ActionTraceActAuthorization(
	@SerializedName("actor") val actor: String,
	@SerializedName("permission") val permission: String
)