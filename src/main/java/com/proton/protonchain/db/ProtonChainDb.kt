package com.proton.protonchain.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.proton.protonchain.model.ChainProvider

@Database(
	entities = [
		ChainProvider::class],
	version = 2,
	exportSchema = false
)
abstract class ProtonChainDb : RoomDatabase() {
	abstract fun chainProviderDao(): ChainProviderDao
}