package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName

data class AccountTotalResources(
	@SerializedName("owner") val owner: String,
	@SerializedName("net_weight") val netWeight: String,
	@SerializedName("cpu_weight") val cpuWeight: String,
	@SerializedName("ram_bytes") val ramBytes: Int
) {
	fun netWeightToDouble(): Double {
		return netWeight.substringBefore(" ").toDouble()
	}

	fun cpuWeightToDouble(): Double {
		return cpuWeight.substringBefore(" ").toDouble()
	}
}