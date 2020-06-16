package com.metallicus.protonsdk.di

import android.content.Context
import com.metallicus.protonsdk.*
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
	WorkerAssistedModule::class,
	WorkerModule::class,
	ProtonModule::class
])
interface ProtonComponent {
	@Component.Builder
	interface Builder {
		@BindsInstance
		fun context(context: Context): Builder

		fun build(): ProtonComponent
	}

	fun inject(protonModule: ProtonModule)

	fun inject(workersModule: WorkersModule)
	fun inject(chainProviderModule: ChainProviderModule)
	fun inject(tokenContractsModule: TokenContractsModule)
	fun inject(accountModule: AccountModule)
	fun inject(currencyBalancesModule: CurrencyBalancesModule)
}
