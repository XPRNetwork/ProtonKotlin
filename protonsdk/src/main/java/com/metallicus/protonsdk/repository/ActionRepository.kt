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
		return protonChainService.getChainInfo("$chainUrl/v1/chain/get_info")
	}

	suspend fun getRequiredKeys(chainUrl: String, requiredKeysBody: RequiredKeysBody): Response<RequiredKeysResponse> {
		return protonChainService.getRequiredKeys("$chainUrl/v1/chain/get_required_keys", requiredKeysBody)
	}

	suspend fun pushTransaction(chainUrl: String, packedTransaction: PackedTransaction): Response<JsonObject> {
		return protonChainService.pushTransaction("$chainUrl/v1/chain/push_transaction", packedTransaction)
	}
}
