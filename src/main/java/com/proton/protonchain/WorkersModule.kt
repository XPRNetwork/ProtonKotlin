package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.*
import com.proton.protonchain.common.Prefs
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.workers.*
import javax.inject.Inject

class WorkersModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var workerFactory: ProtonChainWorkerFactory

	@Inject
	lateinit var prefs: Prefs

	var workManager: WorkManager

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

		val initTokenContractsWorker = OneTimeWorkRequest.Builder(InitTokenContractsWorker::class.java)
			.setConstraints(constraints).build()

		workManager
			.beginUniqueWork(INIT, ExistingWorkPolicy.REPLACE, initChainProviders)
			.then(initTokenContractsWorker)
			.enqueue()
	}

	fun onInitChainProviders(lifecycleOwner: LifecycleOwner, callback: (Boolean) -> Unit) {
		if (prefs.hasChainProviders) {
			callback(true)
		} else {
			val observer = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			observer.observe(lifecycleOwner, Observer { workInfos ->
				if (workInfos.isNotEmpty()) {
					if (workInfos
							.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						callback(false)
						observer.removeObservers(lifecycleOwner)
					} else if (
						workInfos
							.filter { it.tags.contains(InitChainProvidersWorker::class.java.name) }
							.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasChainProviders = true
						callback(true)
						observer.removeObservers(lifecycleOwner)
					}
				}
			})
		}
	}

	fun onInitTokenContracts(lifecycleOwner: LifecycleOwner, callback: (Boolean) -> Unit) {
		if (prefs.hasTokenContracts) {
			callback(true)
		} else {
			val observer = workManager.getWorkInfosForUniqueWorkLiveData(INIT)
			observer.observe(lifecycleOwner, Observer { workInfos ->
				if (workInfos.isNotEmpty()) {
					if (workInfos
							.any { it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED }) {
						callback(false)
						observer.removeObservers(lifecycleOwner)
					} else if (
						workInfos
							.filter { it.tags.contains(InitTokenContractsWorker::class.java.name) }
							.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasTokenContracts = true
						callback(true)
						observer.removeObservers(lifecycleOwner)
					}
				}
			})
		}
	}
}