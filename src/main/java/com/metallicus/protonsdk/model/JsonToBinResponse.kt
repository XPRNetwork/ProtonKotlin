package com.metallicus.protonsdk.model

import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters

@TypeConverters(DefaultTypeConverters::class)
data class JsonToBinResponse(
	@SerializedName("binargs") val binArgs: String,
	@SerializedName("required_scope") val requiredScope: List<String>,
	@SerializedName("required_auth") val requiredAuth: List<String>
)