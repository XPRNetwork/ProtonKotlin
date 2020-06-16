package com.metallicus.protonsdk.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.metallicus.protonsdk.model.AccountContact

object ProtonTypeConverters {
	@TypeConverter
	@JvmStatic
	fun stringToAccountContact(value: String?): AccountContact? {
		val type = object : TypeToken<AccountContact>() {}.type
		return Gson().fromJson(value, type)
	}

	@TypeConverter
	@JvmStatic
	fun accountContactToString(accountContact: AccountContact): String {
		val type = object : TypeToken<AccountContact>() {}.type
		return Gson().toJson(accountContact, type)
	}
}