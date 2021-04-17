/*
 * Copyright (c) 2021 Proton Chain LLC, Delaware
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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