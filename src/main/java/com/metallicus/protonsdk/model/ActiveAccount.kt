package com.metallicus.protonsdk.model

import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey

class ActiveAccount(builder: Builder) {
	val accountName: String = builder.accountName
	val publicKey: String = builder.publicKey
	val privateKey: String = builder.privateKey
	val pin: String = builder.pin

	fun hasPrivateKey(): Boolean {
		return privateKey.isNotEmpty() && pin.isNotEmpty()
	}

	class Builder(val accountName: String) {
		var publicKey: String = ""
		var privateKey: String = ""
		var pin: String = ""

		fun setPrivateKey(privateKeyStr: String, pin: String): Builder {
			val privateKey = EosPrivateKey(privateKeyStr)
			this.publicKey = privateKey.publicKey.toString()

			this.privateKey = privateKeyStr
			this.pin = pin
			return this
		}

		fun create(): ActiveAccount {
			return ActiveAccount(this)
		}
	}
}