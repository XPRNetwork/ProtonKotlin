package com.metallicus.protonsdk.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.metallicus.protonsdk.model.AccountContact
import timber.log.Timber

object DefaultTypeConverters {
	@TypeConverter
	@JvmStatic
	fun stringToIntList(data: String?): List<Int>? {
		return data?.let { str ->
			str.split(",").map {
				try {
					it.toInt()
				} catch (ex: NumberFormatException) {
					Timber.e(ex, "Cannot convert $it to number")
					null
				}
			}
		}?.filterNotNull()
	}

	@TypeConverter
	@JvmStatic
	fun intListToString(ints: List<Int>?): String? {
		return ints?.joinToString(",")
	}

	@TypeConverter
	@JvmStatic
	fun stringToStringList(data: String?): List<String>? {
		return data?.let { str ->
			str.split(",").map { it }
		}
	}

	@TypeConverter
	@JvmStatic
	fun stringListToString(strings: List<String>?): String? {
		return strings?.joinToString(",")
	}

	@TypeConverter
	@JvmStatic
	fun stringToStringDoubleMap(value: String?): Map<String, Double>? {
		val type = object : TypeToken<Map<String, Double>>() {}.type
		return Gson().fromJson(value, type)
	}

	@TypeConverter
	@JvmStatic
	fun stringDoubleMapToString(map: Map<String, Double>): String {
		val type = object : TypeToken<Map<String, Double>>() {}.type
		return Gson().toJson(map, type)
	}

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