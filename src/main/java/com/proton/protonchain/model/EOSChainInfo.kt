package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class EOSChainInfo(
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