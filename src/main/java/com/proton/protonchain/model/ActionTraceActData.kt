package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName

data class ActionTraceActData(
	@SerializedName("from") val from: String? = "",
	@SerializedName("to") val to: String? = "",
	@SerializedName("quantity") val quantity: String? = "",
	@SerializedName("memo") val memo: String? = "",
	@SerializedName("receiver") val receiver: String? = "",
	@SerializedName("stake_cpu_quantity") val stakeCpuQuantity: String? = "0.0",
	@SerializedName("stake_net_quantity") val stakeNetQuantity: String? = "0.0",
	@SerializedName("unstake_cpu_quantity") val unStakeCpuQuantity: String? = "0.0",
	@SerializedName("unstake_net_quantity") val unStakeNetQuantity: String? = "0.0"
) {
	fun quantityToDouble(): Double {
		return quantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun stakeCpuQuantityToDouble(): Double {
		return stakeCpuQuantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun stakeNetQuantityToDouble(): Double {
		return stakeNetQuantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun stakeTotal(): String {
		val cpu = stakeCpuQuantityToDouble()
		val net = stakeNetQuantityToDouble()
		return (cpu.plus(net)).toString()
	}

	fun unStakeCpuQuantityToDouble(): Double {
		return unStakeCpuQuantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun unStakeNetQuantityToDouble(): Double {
		return unStakeNetQuantity?.substringBefore(" ")?.toDouble() ?: 0.0
	}

	fun unStakeTotal(): String {
		val cpu = unStakeCpuQuantityToDouble()
		val net = unStakeNetQuantityToDouble()
		return (cpu.plus(net)).toString()
	}
}