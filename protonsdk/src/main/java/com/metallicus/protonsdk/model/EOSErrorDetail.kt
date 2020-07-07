package com.metallicus.protonsdk.model

import com.google.gson.annotations.SerializedName

data class EOSErrorDetail(
	@SerializedName("message") val message: String,
	@SerializedName("file") val file: String,
	@SerializedName("line_number") val lineNumber: Int,
	@SerializedName("method") val method: String
)