package com.proton.protonchain.repository

import com.google.gson.JsonObject
import com.proton.protonchain.api.ProtonChainService
import com.proton.protonchain.api.TableRowsBody
import com.proton.protonchain.db.TokenContractDao
import com.proton.protonchain.model.TokenContract
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenContractRepository @Inject constructor(
	private val tokenContractDao: TokenContractDao,
	private val protonChainService: ProtonChainService
) {
	fun removeAll() {
		tokenContractDao.removeAll()
	}

	fun addTokenContract(tokenContract: TokenContract) {
		tokenContractDao.insert(tokenContract)
	}

	suspend fun fetchTokenContracts(chainUrl: String, tokensTableScope: String, tokensTableCode: String): Response<JsonObject> {
		return protonChainService.getTableRows("$chainUrl/v1/chain/get_table_rows", TableRowsBody(tokensTableScope, tokensTableCode, "tokens"))
	}

	suspend fun getAllTokenContracts(chainId: String): List<TokenContract> {
		return tokenContractDao.findAllByChainId(chainId)
	}
}
