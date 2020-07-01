package com.metallicus.protonsdk.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.metallicus.protonsdk.model.AccountPermission
import com.metallicus.protonsdk.model.ActionTraceActAuthorization

object EOSTypeConverters {
	@TypeConverter
	@JvmStatic
	fun stringToAccountPermissionList(value: String): List<AccountPermission> {
		val type = object : TypeToken<List<AccountPermission>>() {}.type
		return Gson().fromJson(value, type)
	}

	@TypeConverter
	@JvmStatic
	fun accountPermissionListToString(value: List<AccountPermission>): String {
		val type = object : TypeToken<List<AccountPermission>>() {}.type
		return Gson().toJson(value, type)
	}

	@TypeConverter
	@JvmStatic
	fun stringToActionTraceActAuthorizationList(value: String): List<ActionTraceActAuthorization> {
		val type = object : TypeToken<List<ActionTraceActAuthorization>>() {}.type
		return Gson().fromJson(value, type)
	}

	@TypeConverter
	@JvmStatic
	fun actionTraceActAuthorizationListToString(value: List<ActionTraceActAuthorization>): String {
		val type = object : TypeToken<List<ActionTraceActAuthorization>>() {}.type
		return Gson().toJson(value, type)
	}
}