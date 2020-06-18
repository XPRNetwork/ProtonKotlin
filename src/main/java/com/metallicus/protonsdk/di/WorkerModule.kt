package com.metallicus.protonsdk.di

import androidx.work.WorkerFactory
import com.metallicus.protonsdk.workers.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class WorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(InitChainProviderWorker::class)
    abstract fun bindInitChainProvidersWorker(factory: InitChainProviderWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(InitTokenContractsWorker::class)
    abstract fun bindInitTokenContractsWorker(factory: InitTokenContractsWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(InitActiveAccountWorker::class)
    abstract fun bindInitActiveAccountWorker(factory: InitActiveAccountWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(UpdateTokenContractRatesWorker::class)
    abstract fun bindUpdateTokenContractRatesWorker(factory: UpdateTokenContractRatesWorker.Factory): ChildWorkerFactory

    @Binds
    abstract fun bindWorkerFactory(factory: ProtonWorkerFactory): WorkerFactory
}