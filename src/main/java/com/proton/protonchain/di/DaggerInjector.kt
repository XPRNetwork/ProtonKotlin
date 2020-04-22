package com.proton.protonchain.di

import android.content.Context

class DaggerInjector {
	companion object {
		lateinit var component: ProtonComponent

		fun buildComponent(context: Context): ProtonComponent {
			component = DaggerProtonComponent.builder().context(context).build()
			return component
		}
	}
}