package com.metallicus.protonsdk.model

import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters

@TypeConverters(DefaultTypeConverters::class)
data class KeyAccount(
	@SerializedName("account_names") val accountNames: List<String>
)