package com.metallicus.protonsdk.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.metallicus.protonsdk.db.DefaultTypeConverters
import com.metallicus.protonsdk.db.EOSTypeConverters
import com.metallicus.protonsdk.db.ProtonTypeConverters

@Entity
@TypeConverters(DefaultTypeConverters::class, EOSTypeConverters::class, ProtonTypeConverters::class)
data class Account(
	@PrimaryKey
	@SerializedName("account_name") val accountName: String,

	@SerializedName("head_block_num") val headBlockNum: Int,
	@SerializedName("head_block_time") val headBlockTime: String,
	@SerializedName("privileged") val privileged: Boolean,
	@SerializedName("last_code_update") val lastCodeUpdate: String,
	@SerializedName("created") val created: String,
	@SerializedName("core_liquid_balance") val coreLiquidBalance: String?,
	@SerializedName("ram_quota") val ramQuota: Int,
	@SerializedName("net_weight") val netWeight: Long,
	@SerializedName("cpu_weight") val cpuWeight: Long,
	@SerializedName("ram_usage") val ramUsage: Long,

	@SerializedName("net_limit")
	@Embedded(prefix = "net_limit_") val netLimit: AccountNetLimit,

	@SerializedName("cpu_limit")
	@Embedded(prefix = "cpu_limit_") val cpuLimit: AccountCpuLimit,

	@SerializedName("permissions") val permissions: List<AccountPermission>,

	@SerializedName("total_resources")
	@Embedded(prefix = "total_resources_") val totalResources: AccountTotalResources,

	@SerializedName("self_delegated_bandwidth")
	@Embedded(prefix = "self_delegated_bandwidth_") val selfDelegatedBandwidth: AccountSelfDelegatedBandwidth?,

	@SerializedName("voter_info")
	@Embedded(prefix = "voter_info_") val voterInfo: AccountVoterInfo?
) {
	lateinit var accountChainId: String

	lateinit var accountContact: AccountContact

	fun getBalance(): String {
		return coreLiquidBalance ?: "0"
	}

	fun getDelegatedCPU(): Double {
		var delegatedCPU = 0.00
		if (selfDelegatedBandwidth != null) {
			delegatedCPU =
				totalResources.cpuWeightToDouble() - selfDelegatedBandwidth.cpuWeightToDouble()
		}
		return delegatedCPU
	}

	fun getDelegatedNet(): Double {
		var delegatedNet = 0.00
		if (selfDelegatedBandwidth != null) {
			delegatedNet =
				totalResources.netWeightToDouble() - selfDelegatedBandwidth.netWeightToDouble()
		}
		return delegatedNet
	}

	fun getSelfDelegatedResources(): Double {
		var selfDelegatedResources = 0.00
		if (selfDelegatedBandwidth != null) {
			selfDelegatedResources =
				selfDelegatedBandwidth.cpuWeightToDouble() + selfDelegatedBandwidth.netWeightToDouble()
		}
		return selfDelegatedResources
	}
}