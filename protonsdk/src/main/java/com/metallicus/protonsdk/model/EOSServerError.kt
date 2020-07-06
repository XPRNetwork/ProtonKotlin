package com.metallicus.protonsdk.model

import com.google.gson.annotations.SerializedName

data class EOSServerError(
	@SerializedName("code") val code: Int,
	@SerializedName("message") val message: String,
	@SerializedName("error") val error: EOSError
)