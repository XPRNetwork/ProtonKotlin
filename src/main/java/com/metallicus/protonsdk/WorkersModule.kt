package com.metallicus.protonsdk

import android.content.Context
import androidx.lifecycle.Observer
import androidx.work.*
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.di.DaggerInjector
import com.metallicus.protonsdk.workers.*
import javax.inject.Inject

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
	}

	fun init(chainProviderUrl: String, apiKey: String, apiSecret: String) {
		workManager.pruneWork()

		prefs.clearInit()

		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val chainProviderInputData = Data.Builder()
			.putString(InitChainProviderWorker.CHAIN_PROVIDER_URL, chainProviderUrl)
			.build()

		val initChainProvider = OneTimeWorkRequest.Builder(InitChainProviderWorker::class.java)
			.setConstraints(constraints).setInputData(chainProviderInputData).build()

		val initTokenContracts = OneTimeWorkRequest.Builder(InitTokenContractsWorker::class.java)
			.setConstraints(constraints).build()

		val initActiveAccount = OneTimeWorkRequest.Builder(InitActiveAccountWorker::class.java)
			.setConstraints(constraints).build()

		val initWork = workManager
			.beginUniqueWork(INIT, ExistingWorkPolicy.REPLACE, initChainProvider)

		if (prefs.activeAccountName.isNotEmpty()) {
			initWork
				.then(initTokenContracts)
				.then(initActiveAccount)
				.enqueue()
		} else {
			initWork
				.then(initTokenContracts)
				.enqueue()
		}
	}

	fun onInitChainProvider(callback: (Boolean) -> Unit) {
		if (prefs.hasChainProvider) {
			callback(true)
		} else {
			val workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			val workInfoObserver = object : Observer<List<WorkInfo>> {
				override fun onChanged(workInfos: List<WorkInfo>) {
					val chainProviderWorkInfos =
						workInfos.filter { it.tags.contains(InitChainProviderWorker::class.java.name) }
					if (chainProviderWorkInfos.isEmpty() ||
						workInfos.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						callback(false)
						workInfoLiveData.removeObserver(this)
					} else if (chainProviderWorkInfos.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasChainProvider = true
						callback(true)
						workInfoLiveData.removeObserver(this)
					}
				}
			}
			workInfoLiveData.observeForever(workInfoObserver)
		}
	}

	fun onInitTokenContracts(callback: (Boolean) -> Unit) {
		if (prefs.hasTokenContracts) {
			callback(true)
		} else {
			val workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			val workInfoObserver = object : Observer<List<WorkInfo>> {
				override fun onChanged(workInfos: List<WorkInfo>) {
					val tokenContractWorkInfos =
						workInfos.filter { it.tags.contains(InitTokenContractsWorker::class.java.name) }
					if (tokenContractWorkInfos.isEmpty() ||
						workInfos.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						callback(false)
						workInfoLiveData.removeObserver(this)
					} else if (tokenContractWorkInfos.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasTokenContracts = true
						callback(true)
						workInfoLiveData.removeObserver(this)
					}
				}
			}
			workInfoLiveData.observeForever(workInfoObserver)
		}
	}

	fun onInitActiveAccount(callback: (Boolean) -> Unit) {
		if (prefs.hasActiveAccount) {
			callback(true)
		} else {
			val workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			val workInfoObserver = object : Observer<List<WorkInfo>> {
				override fun onChanged(workInfos: List<WorkInfo>) {
					val activeAccountWorkInfos =
						workInfos.filter { it.tags.contains(InitActiveAccountWorker::class.java.name) }
					if (activeAccountWorkInfos.isEmpty() ||
						workInfos.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						callback(false)
						workInfoLiveData.removeObserver(this)
					} else if (activeAccountWorkInfos.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasActiveAccount = true
						callback(true)
						workInfoLiveData.removeObserver(this)
					}
				}
			}
			workInfoLiveData.observeForever(workInfoObserver)
		}
	}
}