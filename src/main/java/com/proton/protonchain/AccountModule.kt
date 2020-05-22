package com.proton.protonchain

import android.content.Context
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.repository.AccountRepository
import javax.inject.Inject

class AccountModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var accountRepository: AccountRepository

	init {
		DaggerInjector.component.inject(this)
	}
}