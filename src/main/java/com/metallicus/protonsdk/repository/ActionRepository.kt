package com.metallicus.protonsdk.repository

import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.db.AccountContactDao
import com.metallicus.protonsdk.db.ActionDao
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

	suspend fun fetchAccountTokenActions(hyperionHistoryUrl: String, accountName: String, symbol: String) : Response<JsonObject> {
		return protonChainService.getActions("$hyperionHistoryUrl/v2/history/get_actions", accountName, symbol, 250)
	}

	suspend fun getAccountSystemTokenActions(accountName: String, contract: String, symbol: String): List<Action> {
		return actionDao.findBySystemTokenContract(accountName, contract, symbol)
	}

	suspend fun getAccountTokenActions(accountName: String, contract: String, symbol: String): List<Action> {
		return actionDao.findByTokenContract(accountName, contract, symbol)
	}
}
