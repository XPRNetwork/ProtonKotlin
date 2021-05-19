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
			val factoryEntry = workerFactory.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }

			if (factoryEntry != null) {
				val factoryProvider = factoryEntry.value
				factoryProvider.get().create(context, workerParameters)
			} else { // fallback if no factory was found
				val workerClass = Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
				val constructor = workerClass.getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
				constructor.newInstance(context, workerParameters)
			}
		} catch (e: ClassNotFoundException) {
			Timber.d("Worker class not found: $workerClassName")
			null
		}
	}
}