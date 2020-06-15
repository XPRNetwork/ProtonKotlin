package com.metallicus.protonsdk.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.metallicus.protonsdk.model.*

@Database(
	entities = [
		ChainProvider::class,
		TokenContract::class,
		Account::class,
		AccountContact::class,
		CurrencyBalance::class],
	version = 8,
	exportSchema = false
)
abstract class ProtonDb : RoomDatabase() {
	abstract fun chainProviderDao(): ChainProviderDao
	abstract fun tokenContractDao(): TokenContractDao
	abstract fun accountDao(): AccountDao
	abstract fun currencyBalanceDao(): CurrencyBalanceDao
	abstract fun accountContactDao(): AccountContactDao
}