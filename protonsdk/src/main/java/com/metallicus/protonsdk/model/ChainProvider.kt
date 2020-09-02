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

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ChainProvider(
	@PrimaryKey
	@SerializedName("chainId") val chainId: String,

	@SerializedName("name") val name: String,
	@SerializedName("description") val description: String,
	@SerializedName("iconUrl") val iconUrl: String,
	@SerializedName("isTestnet") val isTestnet: Boolean,
	@SerializedName("chainUrl") val chainUrl: String,
	@SerializedName("hyperionHistoryUrl") val hyperionHistoryUrl: String,

	@SerializedName("explorerName") val explorerName: String,
	@SerializedName("explorerUrl") val explorerUrl: String,

	@SerializedName("resourceTokenSymbol") val resourceTokenSymbol: String,
	@SerializedName("resourceTokenContract") val resourceTokenContract: String,
	@SerializedName("systemTokenSymbol") val systemTokenSymbol: String,
	@SerializedName("systemTokenContract") val systemTokenContract: String,

	@SerializedName("createAccountPath") val createAccountPath: String,
	@SerializedName("updateAccountAvatarPath") val updateAccountAvatarPath: String,
	@SerializedName("updateAccountNamePath") val updateAccountNamePath: String,
	@SerializedName("exchangeRatePath") val exchangeRatePath: String
) {
	lateinit var chainApiUrl: String
}