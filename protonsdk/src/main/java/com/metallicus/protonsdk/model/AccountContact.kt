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
package com.metallicus.protonsdk.model

import android.util.Base64
import androidx.annotation.NonNull
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
	var avatar: String = "",
	var verified: Boolean = false
) {
	@NonNull
	lateinit var accountName: String // owner accountName

	fun getDisplayName(): String {
		return name.ifEmpty { id }
	}

	fun getAvatarByteArray(): ByteArray {
		return if (avatar.isEmpty()) ByteArray(0) else Base64.decode(avatar, Base64.DEFAULT)
	}
}