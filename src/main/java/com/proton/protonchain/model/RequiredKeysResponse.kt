package com.proton.protonchain.model

import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.proton.protonchain.db.DefaultTypeConverters

@TypeConverters(DefaultTypeConverters::class)
class RequiredKeysResponse(
	@SerializedName("required_keys") val requiredKeys: List<String>
)