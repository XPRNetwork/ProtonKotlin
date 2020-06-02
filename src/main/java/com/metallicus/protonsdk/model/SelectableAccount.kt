package com.metallicus.protonsdk.model

import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey

data class SelectableAccount(
	val privateKey: EosPrivateKey,
	val accountName: String,
	val chainProvider: ChainProvider)