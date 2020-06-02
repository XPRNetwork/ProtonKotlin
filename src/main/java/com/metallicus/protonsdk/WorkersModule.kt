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

	fun init() {
		workManager.pruneWork()

		prefs.clearInit()

		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val chainProvidersInputData = Data.Builder()
			.putString("chainProvidersUrl", context.getString(R.string.chainProvidersUrl))
			.build()

		val initChainProviders = OneTimeWorkRequest.Builder(InitChainProvidersWorker::class.java)
			.setConstraints(constraints).setInputData(chainProvidersInputData).build()

		val initTokenContracts = OneTimeWorkRequest.Builder(InitTokenContractsWorker::class.java)
			.setConstraints(constraints).build()

		workManager
			.beginUniqueWork(INIT, ExistingWorkPolicy.REPLACE, initChainProviders)
			.then(initTokenContracts)
			.enqueue()
	}

	fun onInitChainProviders(callback: (Boolean) -> Unit) {
		if (prefs.hasChainProviders) {
			callback(true)
		} else {
			val workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			val workInfoObserver = object : Observer<List<WorkInfo>> {
				override fun onChanged(workInfos: List<WorkInfo>) {
					val chainProviderWorkInfos =
						workInfos.filter { it.tags.contains(InitChainProvidersWorker::class.java.name) }
					if (chainProviderWorkInfos.isEmpty() ||
						workInfos.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						callback(false)
						workInfoLiveData.removeObserver(this)
					} else if (chainProviderWorkInfos.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasChainProviders = true
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
}