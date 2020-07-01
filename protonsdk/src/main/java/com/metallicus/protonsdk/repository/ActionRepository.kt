package com.metallicus.protonsdk.repository

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.JsonToBinBody
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.api.RequiredKeysBody
import com.metallicus.protonsdk.db.ActionDao
import com.metallicus.protonsdk.eosio.commander.model.chain.PackedTransaction
import com.metallicus.protonsdk.model.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionRepository @Inject constructor(
	private val actionDao: ActionDao,
	private val protonChainService: ProtonChainService
) {
	suspend fun addAction(action: Action) {
		if (action.isTransfer()) {
			actionDao.insert(action)
		}
	}

	suspend fun fetchAccountTokenActions(hyperionHistoryUrl: String, accountName: String, symbol: String): Response<JsonObject> {
		return protonChainService.getActions("$hyperionHistoryUrl/v2/history/get_actions", accountName, symbol, 250)
	}

	suspend fun getAccountSystemTokenActions(accountName: String, contract: String, symbol: String): List<Action> {
		return actionDao.findBySystemTokenContract(accountName, contract, symbol)
	}

	suspend fun getAccountTokenActions(accountName: String, contract: String, symbol: String): List<Action> {
		return actionDao.findByTokenContract(accountName, contract, symbol)
	}

	suspend fun jsonToBin(chainUrl: String, code: String, action: String, args: JsonElement): Response<JsonToBinResponse> {
		return protonChainService.jsonToBin("$chainUrl/v1/chain/abi_json_to_bin", JsonToBinBody(code, action, args))
	}

	suspend fun getChainInfo(chainUrl: String): Response<ChainInfo> {
		return protonChainService.getChainInfo(chainUrl)
	}

	suspend fun getRequiredKeys(chainUrl: String, requiredKeysBody: RequiredKeysBody): Response<RequiredKeysResponse> {
		return protonChainService.getRequiredKeys("$chainUrl/v1/chain/get_required_keys", requiredKeysBody)
	}

	suspend fun pushTransaction(chainUrl: String, packedTransaction: PackedTransaction): Response<JsonObject> {
		return protonChainService.pushTransaction("$chainUrl/v1/chain/push_transaction", packedTransaction)
	}
}
