package com.metallicus.protonsdk.model

import com.google.gson.annotations.SerializedName

data class EOSError(
	@SerializedName("code") val code: Int,
	@SerializedName("name") val name: String,
	@SerializedName("what") val what: String,
	@SerializedName("details") val details: List<EOSErrorDetail>
)