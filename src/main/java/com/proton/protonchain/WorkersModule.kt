package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.*
import com.proton.protonchain.common.Prefs
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.workers.FetchChainProvidersWorker
import com.proton.protonchain.workers.ProtonChainWorkerFactory
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

	fun prune() {
		workManager.pruneWork()
	}

	fun init() {
		prefs.clearInit()

		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val inputData = Data.Builder()
			.putString("chainProvidersUrl", context.getString(R.string.chainProvidersUrl))
			.build()

		val fetchChainProviders = OneTimeWorkRequest.Builder(FetchChainProvidersWorker::class.java)
			.setConstraints(constraints).setInputData(inputData).build()

		val initWork = workManager
			.beginUniqueWork(INIT, ExistingWorkPolicy.REPLACE, fetchChainProviders)

		initWork
			//.then(fetchChainProviders)
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
							.filter { it.tags.contains(FetchChainProvidersWorker::class.java.name) }
							.all { it.state == WorkInfo.State.SUCCEEDED }) {
						prefs.hasChainProviders = true
						callback(true)
						observer.removeObservers(lifecycleOwner)
					}
				}
			})
		}
	}
}