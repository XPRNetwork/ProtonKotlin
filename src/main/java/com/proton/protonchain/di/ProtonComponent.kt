package com.proton.protonchain.di

import android.content.Context
import com.proton.protonchain.ChainProviderModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
	ProtonModule::class
	//AppModule::class
	//WorkerAssistedModule::class,
	//WorkerModule::class,
	//ActivityModule::class
])
interface ProtonComponent {
	@Component.Builder
	interface Builder {
		@BindsInstance
		fun context(context: Context): Builder

		fun build(): ProtonComponent
	}

	fun inject(protonModule: ProtonModule)

	fun inject(chainProviderModule: ChainProviderModule)
}
