package com.proton.protonchain.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.proton.protonchain.viewmodel.ChainProviderViewModel
import com.proton.protonchain.viewmodel.ViewModelFactory

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
	@Binds
	@IntoMap
	@ViewModelKey(ChainProviderViewModel::class)
	abstract fun bindChainProviderViewModel(chainProviderViewModel: ChainProviderViewModel): ViewModel

	/*@Binds
	@IntoMap
	@ViewModelKey(KeyAccountViewModel::class)
	abstract fun bindKeyAccountViewModel(keyAccountViewModel: KeyAccountViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(AccountViewModel::class)
	abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(TransferViewModel::class)
	abstract fun bindTransferViewModel(transferViewModel: TransferViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(TransactViewModel::class)
	abstract fun bindTransactViewModel(transactViewModel: TransactViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(SigningViewModel::class)
	abstract fun bindSigningViewModel(signingViewModel: SigningViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(StakeViewModel::class)
	abstract fun bindStakeViewModel(stakeViewModel: StakeViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(RAMViewModel::class)
	abstract fun bindRAMViewModel(ramViewModel: RAMViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(DAppViewModel::class)
	abstract fun bindDAppViewModel(dAppViewModel: DAppViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(BackupAccountViewModel::class)
	abstract fun bindBackupAccountViewModel(backupAccountViewModel: BackupAccountViewModel): ViewModel*/

	@Binds
	abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
