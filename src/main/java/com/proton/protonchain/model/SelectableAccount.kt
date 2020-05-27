package com.proton.protonchain.model

import com.proton.protonchain.eosio.commander.ec.EosPrivateKey

data class SelectableAccount(
	val privateKey: EosPrivateKey,
	val accountName: String,
	val chainProvider: ChainProvider)