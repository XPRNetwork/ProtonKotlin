package com.proton.protonchain.api

import androidx.lifecycle.LiveData
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.proton.protonchain.eosio.commander.model.chain.PackedTransaction
import com.proton.protonchain.eosio.commander.model.chain.SignedTransaction
import com.proton.protonchain.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

data class KeyAccountsBody(val public_key: String)
data class CurrencyBalanceBody(val account: String, val code: String, val symbol: String)
data class CurrencyBalancesBody(val account: String, val tokens: List<String>)
data class ActionsBody(val account_name: String, val offset: Int, val pos: Int)
data class AccountBody(val account_name: String)
data class JsonToBinBody(val code: String, val action: String, val args: JsonElement)
data class RequiredKeysBody(val transaction: SignedTransaction, val available_keys: List<String>)
data class TableRowsBody(
	val scope: String,
	val code: String,
	val table: String,
	val lower_bound: String = "",
	val upper_bound: String = "",
	val limit: Long = 1,
	val json: Boolean = true
)

interface EOSService {
	@POST//("/v1/history/get_key_accounts")
	fun getKeyAccounts(
		@Url url: String,
		@Body body: KeyAccountsBody
	): Call<KeyAccount>

	@GET//("/v2/state/get_key_accounts?public_key=")
	fun getStateHistoryKeyAccounts(
		@Url url: String,
		@Query("public_key") publicKey: String
	): Call<KeyAccount>

	@POST//("/v1/history/get_key_accounts")
	suspend fun getKeyAccountsAsync(
		@Url url: String,
		@Body body: KeyAccountsBody
	): Response<KeyAccount>

	@GET//("/v2/state/get_key_accounts?public_key=")
	suspend fun getStateHistoryKeyAccountsAsync(
		@Url url: String,
		@Query("public_key") publicKey: String
	): Response<KeyAccount>

	@POST//("/v1/chain/get_currency_balance")
	suspend fun getCurrencyBalanceAsync(
		@Url url: String,
		@Body body: CurrencyBalanceBody
	): Response<JsonArray>

	@POST//("/v1/chain/get_currency_balances")
	suspend fun getCurrencyBalancesAsync(
		@Url url: String,
		@Body body: CurrencyBalancesBody
	): Response<JsonArray>

	@GET//("/v2/state/get_tokens?account=")
	suspend fun getStateHistoryCurrencyBalancesAsync(
		@Url url: String,
		@Query("account") account: String
	): Response<JsonObject>

	@POST//("/v1/history/get_actions")
	suspend fun getActionsAsync(
		@Url url: String,
		@Body body: ActionsBody
	): Response<JsonObject>

	@GET//("/v2/history/get_actions?account=&transfer.symbol=&filter=&limit=")
	suspend fun getStateHistoryActionsAsync(
		@Url url: String,
		@Query("account") account: String,
		@Query("transfer.symbol") symbol: String,
		//@Query("filter") filter: String,
		@Query("limit") limit: Int
	): Response<JsonObject>

	@POST//("/v1/chain/get_account")
	suspend fun getAccountAsync(
		@Url url: String,
		@Body body: AccountBody
	): Response<Account>

	@POST//("/v1/chain/abi_json_to_bin")
	fun jsonToBin(
		@Url url: String,
		@Body body: JsonToBinBody
	): LiveData<ApiResponse<JsonToBinResponse>>

	@POST//("/v1/chain/get_info")
	fun getChainInfo(@Url url: String): LiveData<ApiResponse<EOSChainInfo>>

	@POST//("/v1/chain/get_required_keys")
	fun getRequiredKeys(
		@Url url: String,
		@Body body: RequiredKeysBody
	): LiveData<ApiResponse<RequiredKeysResponse>>

	@POST//("/v1/chain/push_transaction")
	fun pushTransaction(
		@Url url: String,
		@Body body: PackedTransaction
	): LiveData<ApiResponse<JsonObject>>

	@POST//("/v1/chain/get_table_rows")
	suspend fun getTableRows(
		@Url url: String,
		@Body body: TableRowsBody
	): Response<JsonObject>
}