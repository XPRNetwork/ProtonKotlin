package com.proton.protonchain.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
	AndroidInjectionModule::class,
	AppModule::class
	//WorkerAssistedModule::class,
	//WorkerModule::class,
	//ActivityModule::class
]
)
interface AppComponent {
	@Component.Builder
	interface Builder {
		@BindsInstance
		fun application(application: Application): Builder

		fun build(): AppComponent
	}

	fun inject(application: Application)
}
