package com.metallicus.protonsdk.model

import android.util.Base64
import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import com.metallicus.protonsdk.db.DefaultTypeConverters

@Entity(
	indices = [(Index("id", "accountName"))],
	primaryKeys = ["id", "accountName"]
)
@TypeConverters(DefaultTypeConverters::class)
data class AccountContact(
	val id: String, // accountName
	var name: String = "",
	var avatar: String = ""
) {
	lateinit var accountName: String // owner accountName

	fun getDisplayName(): String {
		return name.ifEmpty { id }
	}

	fun getAvatarByteArray(): ByteArray {
		return if (avatar.isEmpty()) ByteArray(0) else Base64.decode(avatar, Base64.DEFAULT)
	}
}