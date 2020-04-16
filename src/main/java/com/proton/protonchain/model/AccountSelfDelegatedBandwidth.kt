package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName

data class AccountSelfDelegatedBandwidth(
	@SerializedName("from") val from: String,
	@SerializedName("to") val to: String,
	@SerializedName("net_weight") val netWeight: String,
	@SerializedName("cpu_weight") val cpuWeight: String
) {
	fun netWeightToDouble(): Double {
		return netWeight.substringBefore(" ").toDouble()
	}

	fun cpuWeightToDouble(): Double {
		return cpuWeight.substringBefore(" ").toDouble()
	}
}