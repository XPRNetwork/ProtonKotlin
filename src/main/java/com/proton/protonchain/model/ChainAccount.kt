package com.proton.protonchain.model

import androidx.room.*

data class ChainAccount(
	@Embedded
	val account: Account,

	@Relation(
		parentColumn = "accountChainId",
		entityColumn = "chainId"
	)
	val chainProvider: ChainProvider)