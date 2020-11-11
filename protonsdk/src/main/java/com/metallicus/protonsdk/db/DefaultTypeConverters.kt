/*
 * Copyright (c) 2020 Proton Chain LLC, Delaware
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
}