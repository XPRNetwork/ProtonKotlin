/*
 * Copyright (c) 2020 Proton Chain LLC, Delaware
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.metallicus.protonsdk.model

import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey

//fun activeAccount(accountName: String, builder: ActiveAccount.Builder.() -> Unit): ActiveAccount =
//	ActiveAccount.Builder(accountName).apply(builder).create()

class ActiveAccount(builder: Builder) {
	val accountName: String = builder.accountName
	val privateKey: String = builder.privateKey
	val pin: String = builder.pin

	fun hasPrivateKey(): Boolean {
		return privateKey.isNotEmpty() && pin.isNotEmpty()
	}

	fun getPublicKey(): String {
		return EosPrivateKey.getPublicKey(privateKey)
	}

	class Builder(val accountName: String) {
		var privateKey: String = ""
		var pin: String = ""

		fun setPrivateKey(privateKeyStr: String, pin: String): Builder {
			this.privateKey = privateKeyStr
			this.pin = pin
			return this
		}

		fun create(): ActiveAccount {
			return ActiveAccount(this)
		}
	}
}