package com.proton.protonchain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.proton.protonchain.db.DefaultTypeConverters

@Entity(
	indices = [(Index("chainId", "publicKey"))],
	primaryKeys = ["chainId", "publicKey"])
@TypeConverters(DefaultTypeConverters::class)
data class KeyAccount(
	@SerializedName("account_names") val accountNames: List<String>
) {
	lateinit var chainId: String
	lateinit var publicKey: String
}