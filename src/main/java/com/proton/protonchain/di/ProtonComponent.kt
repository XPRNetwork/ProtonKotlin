package com.proton.protonchain.di

import android.content.Context
import com.proton.protonchain.ChainProviderModule
import com.proton.protonchain.WorkersModule
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
}
