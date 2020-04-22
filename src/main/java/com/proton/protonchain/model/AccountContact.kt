package com.proton.protonchain.model

import android.util.Base64
import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import com.proton.protonchain.db.DefaultTypeConverters

@Entity(
	indices = [(Index("id", "chainId", "accountName"))],
	primaryKeys = ["id", "chainId", "accountName"]
)
@TypeConverters(DefaultTypeConverters::class)
data class AccountContact(
	val id: String,
	var name: String = "",
	var avatar: String = "",
	var isLynxChain: Boolean = false
) {
	lateinit var chainId: String
	lateinit var accountName: String

	fun getDisplayName(): String {
		return name.ifEmpty { id }
	}

	fun getAvatarByteArray(): ByteArray {
		return if (avatar.isEmpty()) ByteArray(0) else Base64.decode(avatar, Base64.DEFAULT)
	}
}