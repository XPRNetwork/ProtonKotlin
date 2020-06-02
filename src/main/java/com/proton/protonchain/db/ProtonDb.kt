package com.proton.protonchain.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.proton.protonchain.model.Account
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.TokenContract

@Database(
	entities = [
		ChainProvider::class,
		TokenContract::class,
		Account::class],
	version = 6,
	exportSchema = false
)
abstract class ProtonDb : RoomDatabase() {
	abstract fun chainProviderDao(): ChainProviderDao
	abstract fun tokenContractDao(): TokenContractDao
	abstract fun accountDao(): AccountDao
}