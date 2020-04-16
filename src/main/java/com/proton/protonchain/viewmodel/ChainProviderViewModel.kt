package com.proton.protonchain.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.proton.protonchain.model.ChainProvider
import com.proton.protonchain.repository.ChainProviderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChainProviderViewModel
@Inject constructor(
	application: Application,
	private val chainProviderRepository: ChainProviderRepository
) : AndroidViewModel(application) {

	/*val chainProviders = MutableLiveData<Resource<List<ChainProvider>>>()
	@Suppress("UNCHECKED_CAST")
	fun getChainProviders(shouldFetch: Boolean = true) = viewModelScope.launch {
		chainProviders.value = Resource.loading(null)

		chainProviders.value = withContext(Dispatchers.Default) {
			if (shouldFetch) {
				try {
					val authTask = firebaseAuth.signInAnonymously()
					Tasks.await(authTask, 15, TimeUnit.SECONDS)

					if (authTask.isSuccessful) {
						val chainProvidersSource = TaskCompletionSource<DataSnapshot>()

						val ref = firebaseDatabase.getReference("chain-providers")
						ref.addListenerForSingleValueEvent(object : ValueEventListener {
							override fun onDataChange(dataSnapshot: DataSnapshot) {
								chainProvidersSource.setResult(dataSnapshot)
							}

							override fun onCancelled(databaseError: DatabaseError) {
								chainProvidersSource.setException(databaseError.toException())
							}
						})

						Tasks.await(chainProvidersSource.task, 15, TimeUnit.SECONDS)

						val chainProviders = mutableListOf<ChainProvider>()

						if (chainProvidersSource.task.isSuccessful) {
							chainProviderRepository.removeAll()

							chainProvidersSource.task.result?.children?.forEach {
								val chainProvider = ChainProvider(it.value as Map<String, Any>)
//								if (chainProvider.isActive) {
									chainProviderRepository.addChainProvider(chainProvider)

									chainProviders.add(chainProvider)
//								}
							}

							Resource.success(chainProviders)
						} else {
							Timber.d(chainProvidersSource.task.exception)

							Resource.error(chainProvidersSource.task.exception?.localizedMessage.orEmpty(), null)
						}
					} else {
						Timber.d(authTask.exception)

						Resource.error(authTask.exception?.localizedMessage.orEmpty(), null)
					}
				} catch (e: Exception) {
					Timber.d(e)

					Resource.error("Connection Error or Timeout: Please check your network settings", null)
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
	}*/
}
