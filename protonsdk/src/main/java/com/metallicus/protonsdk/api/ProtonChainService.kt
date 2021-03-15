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
package com.metallicus.protonsdk.api

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.metallicus.protonsdk.eosio.commander.model.chain.PackedTransaction
import com.metallicus.protonsdk.eosio.commander.model.chain.SignedTransaction
import com.metallicus.protonsdk.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class AccountBody(val account_name: String)
enum class TableRowsIndexPosition(val indexPositionName: String) {
	PRIMARY("primary"),
	SECONDARY("secondary"),
	TERTIARY("tertiary"),
	FOURTH("fourth"),
	FIFTH("fifth"),
	SIXTH("sixth"),
	SEVENTH("seventh"),
	EIGHTH("eighth"),
	NINTH("ninth"),
	TENTH("tenth")
}
data class TableRowsBody(
	val scope: String,
	val code: String,
	val table: String,
	val lower_bound: String = "",
	val upper_bound: String = "",
	val limit: Long = 1,
	val index_position: String = TableRowsIndexPosition.PRIMARY.indexPositionName,
	val reverse: Boolean = false,
	val key_type: String = "name",
	val json: Boolean = true)
data class UserNameBody(val name: String)
data class JsonToBinBody(val code: String, val action: String, val args: JsonElement)
data class RequiredKeysBody(val transaction: SignedTransaction, val available_keys: List<String>)

interface ProtonChainService {
	@GET//("/v1/chain/info")
	suspend fun getChainProvider(@Url url: String): Response<JsonObject>

	@GET//("/v1/chain/exchange-rates")
	suspend fun getExchangeRates(@Url url: String): Response<JsonArray>

	@PUT
	suspend fun updateUserName(
		@Url url: String,
		@Header("Authorization") signature: String,
		@Body body: UserNameBody): Response<JsonObject>

	@PUT
	suspend fun uploadUserAvatar(
		@Url url: String,
		@Header("Authorization") signature: String,
		@Body body: MultipartBody): Response<JsonObject>

	@GET//("/v2/state/get_key_accounts?public_key=")
	suspend fun getKeyAccounts(
		@Url url: String,
		@Query("public_key") publicKey: String
	): Response<KeyAccount>

	@POST//("/v1/chain/get_account")
	suspend fun getAccount(
		@Url url: String,
		@Body body: AccountBody
	): Response<Account>

	@POST//("/v1/chain/get_abi")
	suspend fun getAbi(
		@Url url: String,
		@Body body: AccountBody
	): Response<JsonObject>

	@GET//("/v2/state/get_tokens?account=")
	suspend fun getCurrencyBalances(
		@Url url: String,
		@Query("account") account: String
	): Response<JsonObject>

	@GET//("/v2/history/get_actions?account=&transfer.symbol=&filter=&limit=&skip=")
	suspend fun getActions(
		@Url url: String,
		@Query("account") account: String,
		@Query("transfer.symbol") symbol: String,
		//@Query("filter") filter: String,
		@Query("limit") limit: Int,
		@Query("skip") skip: Int
	): Response<JsonObject>

	@POST//("/v1/chain/get_table_rows")
	suspend fun getTableRows(
		@Url url: String,
		@Body body: TableRowsBody
	): Response<JsonObject>

	@POST//("/v1/chain/abi_json_to_bin")
	suspend fun jsonToBin(
		@Url url: String,
		@Body body: JsonToBinBody): Response<JsonToBinResponse>

	@POST//("/v1/chain/get_info")
	suspend fun getChainInfo(@Url url: String): Response<ChainInfo>

	@GET//("/v2/health")
	suspend fun getHealth(@Url url: String): Response<JsonObject>

	@POST//("/v1/chain/get_required_keys")
	suspend fun getRequiredKeys(
		@Url url: String,
		@Body body: RequiredKeysBody): Response<RequiredKeysResponse>

	@POST//("/v1/chain/push_transaction")
	suspend fun pushTransaction(
		@Url url: String,
		@Body body: PackedTransaction): Response<JsonObject>
}