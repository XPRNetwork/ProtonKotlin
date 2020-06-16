package com.metallicus.protonsdk.repository

import com.google.gson.JsonObject
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.api.TableRowsBody
import com.metallicus.protonsdk.db.AccountContactDao
import com.metallicus.protonsdk.model.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountContactRepository @Inject constructor(
	private val accountContactDao: AccountContactDao,
	private val protonChainService: ProtonChainService
) {
	suspend fun addAccountContact(accountContact: AccountContact) {
		accountContactDao.insert(accountContact)
	}

	suspend fun updateAccountContact(accountContact: AccountContact) {
		accountContactDao.update(accountContact)
	}

	suspend fun getAccountContacts(accountName: String): List<AccountContact> {
		return accountContactDao.findByAccountName(accountName)
	}

	suspend fun fetchAccountContact(chainUrl: String, accountName: String, usersInfoTableScope: String, usersInfoTableCode: String, usersInfoTableName: String): Response<JsonObject> {
		return protonChainService.getTableRows("$chainUrl/v1/chain/get_table_rows", TableRowsBody(usersInfoTableScope, usersInfoTableCode, usersInfoTableName, accountName, accountName))
	}
}
