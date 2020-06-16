package com.metallicus.protonsdk

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.metallicus.protonsdk.common.Resource
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.model.*
import com.metallicus.protonsdk.repository.AccountContactRepository
import com.metallicus.protonsdk.repository.AccountRepository
import com.metallicus.protonsdk.repository.ActionRepository
import kotlinx.coroutines.async
import timber.log.Timber
import javax.inject.Inject

class ActionsModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var actionRepository: ActionRepository

	@Inject
	lateinit var accountContactRepository: AccountContactRepository

	init {
		DaggerInjector.component.inject(this)
	}

	suspend fun getActions(chainUrl: String, hyperionHistoryUrl: String, accountName: String, contract: String, symbol: String): Resource<List<Action>> {
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

						val usersInfoTableScope = context.getString(R.string.protonChainUsersInfoTableScope)
						val usersInfoTableCode = context.getString(R.string.protonChainUsersInfoTableCode)
						val usersInfoTableName = context.getString(R.string.protonChainUsersInfoTableName)

						val accountContactResponse = accountContactRepository.fetchAccountContact(chainUrl, accountContactId, usersInfoTableScope, usersInfoTableCode, usersInfoTableName)
						if (accountContactResponse.isSuccessful) {
							val userInfoJsonObject = accountContactResponse.body()

							val rows = userInfoJsonObject?.getAsJsonArray("rows")
							val size = rows?.size() ?: 0
							if (size > 0) {
								val userInfo = rows?.get(0)?.asJsonObject
								accountContact.name = userInfo?.get("name")?.asString.orEmpty()
								accountContact.avatar = userInfo?.get("avatar")?.asString.orEmpty()
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

				Resource.error(errorMsg, null)
			}
		} catch (e: Exception) {
			Resource.error("Connection Error or Timeout: Please check your network settings", null)
		}
	}

	private fun convertStateHistoryAction(stateHistoryActionJson: JsonObject): Action {
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

		return Action(
			actionGlobalActionSeq,
			actionBlockNum,
			actionBlockTime,
			ActionTrace(actionTrxId, actionTraceAct))
	}
}