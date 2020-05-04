package com.proton.protonchain.di

import androidx.work.WorkerFactory
import com.proton.protonchain.workers.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class WorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(FetchChainProvidersWorker::class)
    abstract fun bindFetchChainProvidersWorker(factory: FetchChainProvidersWorker.Factory): ChildWorkerFactory

    @Binds
    abstract fun bindWorkerFactory(factory: ProtonChainWorkerFactory): WorkerFactory
}