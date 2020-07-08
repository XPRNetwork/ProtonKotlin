package com.metallicus.protonsdk.repository

import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.AccountBody
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.api.UserNameBody
import com.metallicus.protonsdk.db.AccountDao
import com.metallicus.protonsdk.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
	private val accountDao: AccountDao,
	private val protonChainService: ProtonChainService
) {
	suspend fun removeAll() {
		accountDao.removeAll()
	}

	suspend fun addAccount(account: Account) {
		accountDao.insert(account)
	}

	suspend fun updateAccount(account: Account) {
		accountDao.update(account)
	}

	suspend fun updateAccountName(updateAccountNameUrl: String, accountName: String, signature: String, name: String): Response<JsonObject> {
		val url = updateAccountNameUrl.replace("{{account}}", accountName)
		return protonChainService.updateUserName(url, "Bearer $signature", UserNameBody(name))
	}

	suspend fun updateAccountAvatar(updateAccountAvatarUrl: String, accountName: String, signature: String, imageByteArray: ByteArray): Response<JsonObject> {
		val url = updateAccountAvatarUrl.replace("{{account}}", accountName)

		val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

		builder
			.addFormDataPart("img", "img.jpeg", imageByteArray.toRequestBody("image/jpeg".toMediaTypeOrNull()))

		val multipartBody = builder.build()

		return protonChainService.uploadUserAvatar(url, "Bearer $signature", multipartBody)
	}

	suspend fun getChainAccount(accountName: String): ChainAccount {
		return accountDao.findByAccountName(accountName)
	}

	suspend fun fetchKeyAccount(hyperionHistoryUrl: String, publicKey: String): Response<KeyAccount> {
		return protonChainService.getKeyAccounts("$hyperionHistoryUrl/v2/state/get_key_accounts", publicKey)
	}

	suspend fun fetchAccount(chainUrl: String, accountName: String): Response<Account> {
		return protonChainService.getAccount("$chainUrl/v1/chain/get_account", AccountBody(accountName))
	}
}
