package com.metallicus.protonsdk.model

import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters

@TypeConverters(DefaultTypeConverters::class)
class RequiredKeysResponse(
	@SerializedName("required_keys") val requiredKeys: List<String>
)