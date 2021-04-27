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
package com.metallicus.protonsdk

import android.content.Context
import androidx.lifecycle.Observer
import androidx.work.*
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.workers.*
import javax.inject.Inject

/**
 * Helper class used to facilitate WorkManager operations
 */
class WorkersModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var workerFactory: ProtonWorkerFactory

	@Inject
	lateinit var prefs: Prefs

	private var workManager: WorkManager

	init {
		DaggerInjector.component.inject(this)

		val workManagerConfig = Configuration.Builder()
			.setWorkerFactory(workerFactory)
			.build()
		WorkManager.initialize(context, workManagerConfig)

		workManager = WorkManager.getInstance(context)
	}

	companion object {
		const val INIT = "WORKER_INIT"
		const val UPDATE_RATES = "WORKER_UPDATE_RATES"
	}

	fun init(protonChainUrl: String) {
		workManager.pruneWork()

		prefs.clearInit()

		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val chainProviderInputData = Data.Builder()
			.putString(InitChainProviderWorker.PROTON_CHAIN_URL, protonChainUrl)
			.build()

		val initChainProvider = OneTimeWorkRequest.Builder(InitChainProviderWorker::class.java)
			.setConstraints(constraints).setInputData(chainProviderInputData).build()

		val initTokenContracts = OneTimeWorkRequest.Builder(InitTokenContractsWorker::class.java)
			.setConstraints(constraints).build()

		val initActiveAccount = OneTimeWorkRequest.Builder(InitActiveAccountWorker::class.java)
			.setConstraints(constraints).build()

		if (prefs.getActiveAccountName().isNotEmpty()) {
			workManager
				.beginUniqueWork(INIT, ExistingWorkPolicy.REPLACE, initChainProvider)
				.then(initTokenContracts)
				.then(initActiveAccount)
				.enqueue()
		} else {
			workManager
				.beginUniqueWork(INIT, ExistingWorkPolicy.REPLACE, initChainProvider)
				.then(initTokenContracts)
				.enqueue()
		}

		// start periodic worker to update exchange rates
		/*val updateTokenContractRates = PeriodicWorkRequest.Builder(UpdateTokenContractRatesWorker::class.java, 15L, TimeUnit.MINUTES)
			.setConstraints(constraints)
			.setInitialDelay(1L, TimeUnit.MINUTES)
			.build()
		workManager.enqueueUniquePeriodicWork(UPDATE_RATES, ExistingPeriodicWorkPolicy.REPLACE, updateTokenContractRates)*/
	}

	fun onInitChainProvider(callback: (Boolean, Data?) -> Unit) {
		if (prefs.hasChainProvider) {
			callback(true, null)
		} else {
			val workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			val workInfoObserver = object : Observer<List<WorkInfo>> {
				override fun onChanged(workInfos: List<WorkInfo>) {
					val chainProviderWorkInfos =
						workInfos.filter { it.tags.contains(InitChainProviderWorker::class.java.name) }
					if (chainProviderWorkInfos.isEmpty() ||
						chainProviderWorkInfos.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						val data = chainProviderWorkInfos.find { it.state == WorkInfo.State.FAILED }?.outputData
						callback(false, data)
						workInfoLiveData.removeObserver(this)
					} else if (chainProviderWorkInfos.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasChainProvider = true
						callback(true, null)
						workInfoLiveData.removeObserver(this)
					}
				}
			}
			workInfoLiveData.observeForever(workInfoObserver)
		}
	}

	fun onInitTokenContracts(callback: (Boolean, Data?) -> Unit) {
		if (prefs.hasTokenContracts) {
			callback(true, null)
		} else {
			val workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			val workInfoObserver = object : Observer<List<WorkInfo>> {
				override fun onChanged(workInfos: List<WorkInfo>) {
					val tokenContractWorkInfos =
						workInfos.filter { it.tags.contains(InitTokenContractsWorker::class.java.name) }
					if (tokenContractWorkInfos.isEmpty() ||
						tokenContractWorkInfos.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						val data = tokenContractWorkInfos.find { it.state == WorkInfo.State.FAILED }?.outputData
						callback(false, data)
						workInfoLiveData.removeObserver(this)
					} else if (tokenContractWorkInfos.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasTokenContracts = true
						callback(true, null)
						workInfoLiveData.removeObserver(this)
					}
				}
			}
			workInfoLiveData.observeForever(workInfoObserver)
		}
	}

	fun onInitActiveAccount(callback: (Boolean, Data?) -> Unit) {
		if (prefs.hasActiveAccount) {
			callback(true, null)
		} else {
			val workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			val workInfoObserver = object : Observer<List<WorkInfo>> {
				override fun onChanged(workInfos: List<WorkInfo>) {
					val activeAccountWorkInfos =
						workInfos.filter { it.tags.contains(InitActiveAccountWorker::class.java.name) }
					if (activeAccountWorkInfos.isEmpty() ||
						activeAccountWorkInfos.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						val data = activeAccountWorkInfos.find { it.state == WorkInfo.State.FAILED }?.outputData
						callback(false, data)
						workInfoLiveData.removeObserver(this)
					} else if (activeAccountWorkInfos.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasActiveAccount = true
						callback(true, null)
						workInfoLiveData.removeObserver(this)
					}
				}
			}
			workInfoLiveData.observeForever(workInfoObserver)
		}
	}
}