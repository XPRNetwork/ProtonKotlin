package com.proton.protonchain.model

import com.google.gson.annotations.SerializedName

data class AccountVoterInfo(
	@SerializedName("owner") val owner: String,
	@SerializedName("proxy") val proxy: String,
	@SerializedName("staked") val staked: Long,
	@SerializedName("last_vote_weight") val lastVoteWeight: String,
	@SerializedName("proxied_vote_weight") val proxiedVoteWeight: String,
	@SerializedName("is_proxy") val isProxy: Int
)