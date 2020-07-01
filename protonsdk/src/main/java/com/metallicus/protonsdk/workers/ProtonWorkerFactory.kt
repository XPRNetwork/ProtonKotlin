package com.metallicus.protonsdk.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class ProtonWorkerFactory @Inject constructor(
	private val workerFactory: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>
) : WorkerFactory() {
	override fun createWorker(
		context: Context,
		workerClassName: String,
		workerParameters: WorkerParameters
	): ListenableWorker? {
		return try {
			val factoryEntry =
				workerFactory.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }

			if (factoryEntry != null) {
				val factoryProvider = factoryEntry.value
				factoryProvider.get().create(context, workerParameters)
			} else { // fallback if no factory was found
				val workerClass = Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
				val constructor =
					workerClass.getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
				constructor.newInstance(context, workerParameters)
			}
		} catch (e: ClassNotFoundException) {
			Timber.d("Worker class not found: $workerClassName")
			null
		}
	}
}