/*
 * Copyright (c) 2021 Proton Chain LLC, Delaware
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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