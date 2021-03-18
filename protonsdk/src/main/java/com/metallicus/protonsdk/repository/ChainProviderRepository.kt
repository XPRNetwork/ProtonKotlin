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

import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.AccountBody
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.api.TableRowsBody
import com.metallicus.protonsdk.api.TableRowsIndexPosition
import com.metallicus.protonsdk.db.ChainProviderDao
import com.metallicus.protonsdk.model.ChainInfo
import com.metallicus.protonsdk.model.ChainProvider
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChainProviderRepository @Inject constructor(
	private val chainProviderDao: ChainProviderDao,
	private val protonChainService: ProtonChainService
) {
	suspend fun removeAll() {
		chainProviderDao.removeAll()
	}

	suspend fun addChainProvider(chainProvider: ChainProvider) {
		chainProviderDao.insert(chainProvider)
	}

	suspend fun updateChainUrl(chainId: String, chainUrl: String) {
		chainProviderDao.updateChainUrl(chainId, chainUrl)
	}

	suspend fun updateHyperionHistoryUrl(chainId: String, hyperionHistory: String) {
		chainProviderDao.updateHyperionHistoryUrl(chainId, hyperionHistory)
	}

	suspend fun fetchChainProvider(protonChainUrl: String): Response<JsonObject> {
		return protonChainService.getChainProvider("$protonChainUrl/v1/chain/info")
	}

	suspend fun fetchKYCProviders(chainUrl: String, kycProvidersTableScope: String, kycProvidersTableCode: String, kycProvidersTableName: String): Response<JsonObject> {
		return protonChainService.getTableRows("$chainUrl/v1/chain/get_table_rows", TableRowsBody(kycProvidersTableScope, kycProvidersTableCode, kycProvidersTableName, "", "", 100))
	}

	suspend fun getChainProvider(id: String): ChainProvider {
		return chainProviderDao.findById(id)
	}

	suspend fun getChainInfo(chainUrl: String): Response<ChainInfo> {
		return protonChainService.getChainInfo("$chainUrl/v1/chain/get_info")
	}

	suspend fun getHealth(chainUrl: String): Response<JsonObject> {
		return protonChainService.getHealth("$chainUrl/v2/health")
	}

	suspend fun getAbi(chainUrl: String, accountName: String): Response<JsonObject> {
		return protonChainService.getAbi("$chainUrl/v1/chain/get_abi", AccountBody(accountName))
	}

	suspend fun getTableRows(
		chainUrl: String,
		scope: String,
		code: String,
		name: String,
		lowerBound: String = "",
		upperBound: String = "",
		limit: Long = 1,
		indexPosition: String = TableRowsIndexPosition.PRIMARY.indexPositionName,
		reverse: Boolean = false
	): Response<JsonObject> {
		return protonChainService.getTableRows(
			"$chainUrl/v1/chain/get_table_rows",
			TableRowsBody(scope, code, name, lowerBound, upperBound, limit, indexPosition, reverse))
	}
}
