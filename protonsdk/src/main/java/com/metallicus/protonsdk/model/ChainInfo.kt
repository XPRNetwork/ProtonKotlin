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

import com.google.gson.annotations.SerializedName
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class ChainInfo(
	@SerializedName("server_version") val serverVersion: String,
	@SerializedName("chain_id") val chainId: String,
	@SerializedName("head_block_num") val headBlockNum: Int,
	@SerializedName("last_irreversible_block_num") val lastIrreversibleBlockNum: Int,
	@SerializedName("last_irreversible_block_id") val lastIrreversibleBlockId: String,
	@SerializedName("head_block_id") val headBlockId: String,
	@SerializedName("head_block_time") val headBlockTime: String,
	@SerializedName("head_block_producer") val headBlockProducer: String,
	@SerializedName("virtual_block_cpu_limit") val virtualBlockCpuLimit: Int,
	@SerializedName("virtual_block_net_limit") val virtualBlockNetLimit: Int,
	@SerializedName("block_cpu_limit") val blockCpuLimit: Int,
	@SerializedName("block_net_limit") val blockNetLimit: Int
) {
	fun getTimeAfterHeadBlockTime(diffInMilSec: Int): String {
		val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
		return try {
			sdf.timeZone = TimeZone.getTimeZone("UTC")

			val date = sdf.parse(headBlockTime)
			date?.let {
				val c = Calendar.getInstance()
				c.time = it
				c.add(Calendar.MILLISECOND, diffInMilSec)
				sdf.format(c.time)
			} ?: headBlockTime
		} catch (e: ParseException) {
			e.printStackTrace()

			headBlockTime
		}
	}
}