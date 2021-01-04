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
package com.metallicus.protonsdk

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.common.SecureKeys
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.eosio.commander.ec.EosPrivateKey
import com.metallicus.protonsdk.eosio.commander.model.chain.Action
import com.metallicus.protonsdk.eosio.commander.model.chain.PackedTransaction
import com.metallicus.protonsdk.eosio.commander.model.chain.SignedTransaction
import com.metallicus.protonsdk.eosio.commander.model.types.EosTransfer
import com.metallicus.protonsdk.eosio.commander.model.types.TypeChainId
import com.metallicus.protonsdk.model.*
import com.metallicus.protonsdk.model.Action as AccountAction
import com.metallicus.protonsdk.repository.AccountContactRepository
import com.metallicus.protonsdk.repository.ActionRepository
import com.metallicus.protonsdk.repository.ChainProviderRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Helper class used for [Action] based operations
 */
class ActionsModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var chainProviderRepository: ChainProviderRepository

	@Inject
	lateinit var actionRepository: ActionRepository

	@Inject
	lateinit var accountContactRepository: AccountContactRepository

	@Inject
	lateinit var prefs: Prefs

	@Inject
	lateinit var secureKeys: SecureKeys

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getActions(chainUrl: String, hyperionHistoryUrl: String, accountName: String, contract: String, symbol: String): Resource<List<AccountAction>> {
		return try {
			val response = actionRepository.fetchAccountTokenActions(hyperionHistoryUrl, accountName, symbol)
			if (response.isSuccessful) {
				val jsonObject = response.body()

				val actions = jsonObject?.getAsJsonArray("actions")

				actions?.forEach {
					try {
						val action = convertStateHistoryAction(it.asJsonObject)

						action.accountName = accountName

						val toAccount = action.actionTrace.act.data?.to.orEmpty()
						val fromAccount = action.actionTrace.act.data?.from.orEmpty()

						val accountContactId = if (action.isSender()) toAccount else fromAccount

						val accountContact = AccountContact(accountContactId)
						accountContact.accountName = accountName

						// add appropriate account contacts
						if (action.isSender() &&
							toAccount != "eosio.stake" &&
							toAccount != "eosio.ramfee" &&
							toAccount != "eosio.ram") {
							accountContactRepository.addAccountContact(accountContact)
						}

						val usersInfoTableScope = context.getString(R.string.usersInfoTableScope)
						val usersInfoTableCode = context.getString(R.string.usersInfoTableCode)
						val usersInfoTableName = context.getString(R.string.usersInfoTableName)

						val accountContactResponse = accountContactRepository.fetchAccountContact(chainUrl, accountContactId, usersInfoTableScope, usersInfoTableCode, usersInfoTableName)
						if (accountContactResponse.isSuccessful) {
							val userInfoJsonObject = accountContactResponse.body()

							val rows = userInfoJsonObject?.getAsJsonArray("rows")
							val size = rows?.size() ?: 0
							if (size > 0) {
								val userInfo = rows?.get(0)?.asJsonObject
								accountContact.name = userInfo?.get("name")?.asString.orEmpty()
								accountContact.avatar = userInfo?.get("avatar")?.asString.orEmpty()
								val verifiedInt = userInfo?.get("verified")?.asInt ?: 0
								accountContact.verified = verifiedInt == 1
							}
						} else {
							val msg = response.errorBody()?.string()
							val errorMsg = if (msg.isNullOrEmpty()) {
								response.message()
							} else {
								msg
							}

							Timber.d(errorMsg)
						}

						accountContactRepository.updateAccountContact(accountContact)

						action.accountContact = accountContact

						actionRepository.addAction(action)
					} catch (e: Exception) {
						Timber.d("%s - %s", e.localizedMessage, it.toString())
					}
				}

				val accountTokenActions = if (contract == "eosio.token") {
					actionRepository.getAccountSystemTokenActions(accountName, contract, symbol)
				} else {
					actionRepository.getAccountTokenActions(accountName, contract, symbol)
				}

				Resource.success(accountTokenActions)
			} else {
				val msg = response.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					response.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	private fun convertStateHistoryAction(stateHistoryActionJson: JsonObject): AccountAction {
		val actionGlobalActionSeq = stateHistoryActionJson.get("global_sequence").asLong.toInt()
		val actionBlockNum = stateHistoryActionJson.get("block_num").asLong.toInt()
		val actionBlockTime = stateHistoryActionJson.get("@timestamp").asString
		val actionTrxId = stateHistoryActionJson.get("trx_id").asString

		val actionActJson = stateHistoryActionJson.getAsJsonObject("act")

		val actionTraceActAccount = actionActJson.get("account").asString
		val actionTraceActName = actionActJson.get("name").asString

		val actionTraceActAuthorizationList = mutableListOf<ActionTraceActAuthorization>()
		val actionActAuthorizationJsonArray = actionActJson.getAsJsonArray("authorization")
		actionActAuthorizationJsonArray.forEach {
			val actionTraceActAuthorization = Gson().fromJson(it, ActionTraceActAuthorization::class.java)
			actionTraceActAuthorizationList.add(actionTraceActAuthorization)
		}

		val actionTraceActDataJson = actionActJson.getAsJsonObject("data")
		val actionTraceActDataFrom = actionTraceActDataJson.get("from").asString
		val actionTraceActDataTo = actionTraceActDataJson.get("to").asString
		val actionTraceActDataQuantity = if (actionTraceActDataJson.has("quantity")) {
			actionTraceActDataJson.get("quantity").asString
		} else {
			val actionTraceActDataAmount = actionTraceActDataJson.get("amount").asString
			val actionTraceActDataSymbol = actionTraceActDataJson.get("symbol").asString
			"$actionTraceActDataAmount $actionTraceActDataSymbol"
		}
		val actionTraceActDataMemo = actionTraceActDataJson.get("memo").asString

		val actionTraceActData = ActionTraceActData(
			actionTraceActDataFrom,
			actionTraceActDataTo,
			actionTraceActDataQuantity,
			actionTraceActDataMemo)

		val actionTraceAct = ActionTraceAct(
			actionTraceActAccount,
			actionTraceActName,
			actionTraceActAuthorizationList,
			actionTraceActData)

		return AccountAction(
			actionGlobalActionSeq,
			actionBlockNum,
			actionBlockTime,
			ActionTrace(actionTrxId, actionTraceAct))
	}

	suspend fun transferTokens(chainUrl: String, pin: String, contract: String, from: String,
							   to: String, quantity: String, memo: String): Resource<JsonObject> {
		return try {
			val eosTransfer = EosTransfer(from, to, quantity, memo)
			val jsonToBinArgs = eosTransfer.jsonToBinArgs()

			val action = Action(contract, eosTransfer.action)
			action.setAuthorization(eosTransfer.activePermission)

			val jsonToBinResponse =
				actionRepository.jsonToBin(
					chainUrl,
					contract,
					eosTransfer.action,
					jsonToBinArgs)
			if (jsonToBinResponse.isSuccessful) {
				val jsonToBin = jsonToBinResponse.body()

				requireNotNull(jsonToBin)

				action.setData(jsonToBin.binArgs)

				val signedTransaction = SignedTransaction()
				signedTransaction.addAction(action)

				signAndPushTransaction(chainUrl, pin, signedTransaction)
			} else {
				val msg = jsonToBinResponse.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					jsonToBinResponse.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}

	suspend fun pushTransactions(chainUrl: String, pin: String, actions: List<Action>): Resource<JsonObject> {
		try {
			if (actions.isNotEmpty()) {
				actions.forEach { action ->
					val jsonToBinResponse = actionRepository.jsonToBin(
						chainUrl,
						action.account,
						action.name,
						JsonParser.parseString(action.data.asString)
					)
					if (jsonToBinResponse.isSuccessful) {
						val jsonToBin = jsonToBinResponse.body()

						requireNotNull(jsonToBin)

						action.setData(jsonToBin.binArgs)
					} else {
						val msg = jsonToBinResponse.errorBody()?.string()
						val errorMsg = if (msg.isNullOrEmpty()) {
							jsonToBinResponse.message()
						} else {
							msg
						}

						return Resource.error(errorMsg)
					}
				}
			} else {
				return Resource.error("No Actions")
			}
		} catch (e: Exception) {
			return Resource.error(e.localizedMessage.orEmpty())
		}

		val signedTransaction = SignedTransaction()
		signedTransaction.actions = actions
		return signAndPushTransaction(chainUrl, pin, signedTransaction)
	}

	private suspend fun signAndPushTransaction(chainUrl: String, pin: String, signedTransaction: SignedTransaction): Resource<JsonObject> {
		return try {
			val chainInfoResponse = chainProviderRepository.getChainInfo(chainUrl)
			if (chainInfoResponse.isSuccessful) {
				val chainInfo = chainInfoResponse.body()

				requireNotNull(chainInfo)

				signedTransaction.setReferenceBlock(chainInfo.headBlockId)
				signedTransaction.expiration = chainInfo.getTimeAfterHeadBlockTime(30000)

				val publicKey = prefs.getActivePublicKey()

				val privateKeyStr = secureKeys.getPrivateKey(publicKey, pin)

				require(privateKeyStr != null && privateKeyStr != "") { "No private key found" }

				val privateKey = EosPrivateKey(privateKeyStr)

				signedTransaction.sign(privateKey, TypeChainId(chainInfo.chainId))

				val packedTransaction = PackedTransaction(signedTransaction)

				val pushTransactionResponse = actionRepository.pushTransaction(chainUrl, packedTransaction)
				if (pushTransactionResponse.isSuccessful) {
					Resource.success(pushTransactionResponse.body())
				} else {
					val msg = pushTransactionResponse.errorBody()?.string()
					val errorMsg = if (msg.isNullOrEmpty()) {
						pushTransactionResponse.message()
					} else {
						msg
					}

					Resource.error(errorMsg)
				}
			} else {
				val msg = chainInfoResponse.errorBody()?.string()
				val errorMsg = if (msg.isNullOrEmpty()) {
					chainInfoResponse.message()
				} else {
					msg
				}

				Resource.error(errorMsg)
			}
		} catch (e: Exception) {
			Resource.error(e.localizedMessage.orEmpty())
		}
	}
}