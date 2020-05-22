package com.proton.protonchain.repository

import com.proton.protonchain.api.EOSService
import com.proton.protonchain.api.ProtonChainService
import com.proton.protonchain.db.AccountDao
import com.proton.protonchain.model.Account
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
	private val accountDao: AccountDao,
	private val eosService: EOSService,
	private val protonChainService: ProtonChainService
) {
	fun addAccount(account: Account) {
		accountDao.insert(account)
	}

	fun updateAccount(account: Account) {
		accountDao.update(account)
	}
}
