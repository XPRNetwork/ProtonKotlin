package com.proton.protonchain

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proton.protonchain.di.DaggerInjector
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.Resource
import com.proton.protonchain.repository.ChainProviderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ChainProviderModule {
	@Inject
	lateinit var context: Context

	@Inject
	lateinit var chainProviderRepository: ChainProviderRepository

	private var chainProvidersUrl: String

	init {
		DaggerInjector.component.inject(this)
		chainProvidersUrl = context.getString(R.string.chainProvidersUrl)
	}

	val chainProviders = MutableLiveData<Resource<List<ChainProvider>>>()
	fun getChainProviders(shouldFetch: Boolean = true) = GlobalScope.launch {
		chainProviders.postValue(Resource.loading(null))

		chainProviders.postValue(withContext(Dispatchers.Default) {
			if (shouldFetch) {
				try {
					val response = chainProviderRepository.fetchChainProviders(chainProvidersUrl)
					if (response.isSuccessful) {

						// TODO: look at JsonArray, add chain provider to db, and return list of chain providers

						val gson = Gson()
						val chainProviders = mutableListOf<ChainProvider>()
						response.body()?.entrySet()?.forEach { entry ->
							val type = object : TypeToken<Map<String, Any>>() {}.type
							val chainProviderMap = gson.fromJson<Map<String, Any>>(entry.value, type)
							val chainProvider = ChainProvider(chainProviderMap)
							chainProviders.add(chainProvider)
						}

						Resource.success(chainProviders)
					} else {
						val msg = response.errorBody()?.string()
						val errorMsg = if (msg.isNullOrEmpty()) {
							response.message()
						} else {
							msg
						}
						Resource.error(errorMsg, null)
					}
				} catch (e: Exception) {
					Timber.d(e)

					Resource.error(
						"Connection Error or Timeout: Please check your network settings",
						null
					)
				}
			} else {
				val chainProviders = chainProviderRepository.getAllChainProviders()

				Resource.success(chainProviders)
			}
		})
	}

	val chainProvider = MutableLiveData<ChainProvider>()
	fun getChainProvider(id: String) = GlobalScope.launch {
		chainProvider.postValue(withContext(Dispatchers.Default) {
			chainProviderRepository.getChainProvider(id)
		})
	}
}