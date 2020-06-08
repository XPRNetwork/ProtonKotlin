package com.metallicus.protonsdk.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.metallicus.protonsdk.model.Account
import com.metallicus.protonsdk.model.ChainProvider
import com.metallicus.protonsdk.model.TokenContract

@Database(
	entities = [
		ChainProvider::class,
		TokenContract::class,
		Account::class],
	version = 7,
	exportSchema = false
)
abstract class ProtonDb : RoomDatabase() {
	abstract fun chainProviderDao(): ChainProviderDao
	abstract fun tokenContractDao(): TokenContractDao
	abstract fun accountDao(): AccountDao
}