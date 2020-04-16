package com.proton.protonchain.di

import android.app.Application
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.proton.protonchain.R
import com.proton.protonchain.api.EOSChainService
import com.proton.protonchain.api.LiveDataCallAdapterFactory
import com.proton.protonchain.api.ProtonChainService
import com.proton.protonchain.db.ChainProviderDao
import com.proton.protonchain.db.ProtonChainDb
import com.proton.protonchain.eosio.commander.GsonEosTypeAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
	@Singleton
	@Provides
	fun provideGson(): Gson {
		return GsonBuilder()
			.registerTypeAdapterFactory(GsonEosTypeAdapterFactory())
//			.registerTypeAdapter(ActionTraceAct::class.java, ActionTraceActTypeAdapter())
			.serializeNulls()
//			.excludeFieldsWithoutExposeAnnotation()
			.create()
	}

	@Singleton
	@Provides
	fun provideDb(app: Application): ProtonChainDb {
		return Room
			.databaseBuilder(app, ProtonChainDb::class.java, "protonchain.db")
			.fallbackToDestructiveMigration()
			.build()
	}

	@Singleton
	@Provides
	fun provideChainProviderDao(db: ProtonChainDb): ChainProviderDao {
		return db.chainProviderDao()
	}

	@Singleton
	@Provides
	fun provideChainService(app: Application, gson: Gson): EOSChainService {
//		val logging = HttpLoggingInterceptor()
//		logging.level = HttpLoggingInterceptor.Level.BODY
//		val httpClient = OkHttpClient.Builder()
//		httpClient.addInterceptor(logging)

		val httpClient = OkHttpClient.Builder()
			.callTimeout(30, TimeUnit.SECONDS)
			.connectTimeout(30, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(30, TimeUnit.SECONDS)

		return Retrofit.Builder()
			.baseUrl(app.getString(R.string.defaultEOSChainUrl))
			.addConverterFactory(GsonConverterFactory.create(gson))
			.addCallAdapterFactory(LiveDataCallAdapterFactory())
			.client(httpClient.build())
			.build()
			.create(EOSChainService::class.java)
	}

	@Singleton
	@Provides
	fun provideProtonChainService(app: Application, gson: Gson): ProtonChainService {
//		val logging = HttpLoggingInterceptor()
//		logging.level = HttpLoggingInterceptor.Level.BODY
//		val httpClient = OkHttpClient.Builder()
//		httpClient.addInterceptor(logging)

		val httpClient = OkHttpClient.Builder()
			.callTimeout(30, TimeUnit.SECONDS)
			.connectTimeout(30, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(30, TimeUnit.SECONDS)

		return Retrofit.Builder()
			.baseUrl(app.getString(R.string.defaultProtonChainUrl))
			.addConverterFactory(GsonConverterFactory.create(gson))
			.addCallAdapterFactory(LiveDataCallAdapterFactory())
			.client(httpClient.build())
			.build()
			.create(ProtonChainService::class.java)
	}

/*	@Singleton
	@Provides
	fun provideFirebaseFunctionsService(app: Application): FirebaseFunctionsService {
		val userAgent =
			app.getString(R.string.appName) + "/" + BuildConfig.VERSION_NAME + " (" + BuildConfig.APPLICATION_ID + "; " + Build.MANUFACTURER + "/" + Build.MODEL + "; Android " + Build.VERSION.SDK_INT + ") okhttp"

		val httpClient = OkHttpClient.Builder()
			.callTimeout(30, TimeUnit.SECONDS)
			.connectTimeout(30, TimeUnit.SECONDS) // this timeout is required for account creation
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(30, TimeUnit.SECONDS)
			.addInterceptor {
				val originalRequest = it.request()
				val requestWithUserAgent = originalRequest.newBuilder()
					.header("User-Agent", userAgent)
					.build()
				it.proceed(requestWithUserAgent)
			}

		return Retrofit.Builder()
			.baseUrl(app.getString(R.string.firebaseFunctionsUrl))
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(LiveDataCallAdapterFactory())
			.client(httpClient.build())
			.build()
			.create(FirebaseFunctionsService::class.java)
	}

	@Singleton
	@Provides
	fun provideGooglePurchasesService(app: Application): GooglePurchasesService {
		val logging = HttpLoggingInterceptor()
		logging.level = HttpLoggingInterceptor.Level.BODY
		val httpClient = OkHttpClient.Builder()
		httpClient.addInterceptor(logging)

		return Retrofit.Builder()
			.baseUrl(app.getString(R.string.googlePurchasesApi))
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(LiveDataCallAdapterFactory())
			.client(httpClient.build())
			.build()
			.create(GooglePurchasesService::class.java)
	}

	@Singleton
	@Provides
	fun provideFirebaseAuth(): FirebaseAuth {
		return FirebaseAuth.getInstance()
	}

	@Singleton
	@Provides
	fun provideFirebaseStorage(app: Application): FirebaseStorage {
		return FirebaseStorage.getInstance(app.getString(R.string.firebaseStorageUrl))
	}

	@Singleton
	@Provides
	fun provideFirebaseDatabase(): FirebaseDatabase {
		return FirebaseDatabase.getInstance()
	}

	@Singleton
	@Provides
	fun provideAlgoliaClientSearch(app: Application): ClientSearch {
		val appID = ApplicationID(app.getString(R.string.algoliaAppId))
		val apiKey = APIKey(app.getString(R.string.algoliaApiKey))

		return ClientSearch(
			ConfigurationSearch(
				applicationID = appID,
				apiKey = apiKey,
				engine = OkHttp.create {
				}
			)
		)
	}

	@Singleton
	@Provides
	fun provideGlideRequestManager(app: Application): RequestManager {
		return Glide.with(app)
	}

	@Singleton
	@Provides
	fun provideKeyAccountDao(db: LynxWalletDb): KeyAccountDao {
		return db.keyAccountDao()
	}

	@Singleton
	@Provides
	fun provideAccountDao(db: LynxWalletDb): AccountDao {
		return db.accountDao()
	}

	@Singleton
	@Provides
	fun provideActionDao(db: LynxWalletDb): ActionDao {
		return db.actionDao()
	}

	@Singleton
	@Provides
	fun provideDAppDao(db: LynxWalletDb): DAppDao {
		return db.dAppDao()
	}

	@Singleton
	@Provides
	fun provideTokenContractDao(db: LynxWalletDb): TokenContractDao {
		return db.tokenContractDao()
	}

	@Singleton
	@Provides
	fun provideCurrencyBalanceDao(db: LynxWalletDb): CurrencyBalanceDao {
		return db.currencyBalanceDao()
	}

	@Singleton
	@Provides
	fun provideAccountContactDao(db: LynxWalletDb): AccountContactDao {
		return db.accountContactDao()
	}

	@Singleton
	@Provides
	fun providePrefs(app: Application): Prefs {
		return Prefs(app)
	}

	@Singleton
	@Provides
	fun provideAccountPrefs(app: Application): AccountPrefs {
		return AccountPrefs(app)
	}

	@Singleton
	@Provides
	fun provideAppUpdateManager(app: Application): AppUpdateManager {
		return AppUpdateManagerFactory.create(app)
	}

	@Singleton
	@Provides
	fun provideWorkManager(app: Application): WorkManager {
		return WorkManager.getInstance(app)
	}

	@Singleton
	@Provides
	fun provideWorkers(workManager: WorkManager, prefs: Prefs, accountPrefs: AccountPrefs): Workers {
		return Workers(workManager, prefs, accountPrefs)
	}*/
}
