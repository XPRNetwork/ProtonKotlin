package com.proton.protonchain.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.proton.protonchain.R
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.model.Resource
import com.proton.protonchain.repository.ChainProviderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ChainProviderViewModel
@Inject constructor(
	application: Application,
	private val chainProviderRepository: ChainProviderRepository
) : AndroidViewModel(application) {

	private val chainProvidersUrl = application.getString(R.string.chainProvidersUrl)

	val chainProviders = MutableLiveData<Resource<List<ChainProvider>>>()
	fun getChainProviders(shouldFetch: Boolean = true) = viewModelScope.launch {
		chainProviders.value = Resource.loading(null)

		chainProviders.value = withContext(Dispatchers.Default) {
			if (shouldFetch) {
				try {
					val response = chainProviderRepository.fetchChainProviders(chainProvidersUrl)
					if (response.isSuccessful) {

						// TODO: look at JsonArray, add chain provider to db, and return list of chain providers
						Resource.success(null)

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
		}
	}

	val chainProvider = MutableLiveData<ChainProvider>()
	fun getChainProvider(id: String) = viewModelScope.launch {
		chainProvider.value = withContext(Dispatchers.Default) {
			chainProviderRepository.getChainProvider(id)
		}
	}
}
