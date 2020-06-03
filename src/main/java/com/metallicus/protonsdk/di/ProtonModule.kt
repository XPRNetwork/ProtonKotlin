package com.metallicus.protonsdk.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.metallicus.protonsdk.R
import com.metallicus.protonsdk.api.LiveDataCallAdapterFactory
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.common.SecureKeys
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.db.AccountDao
import com.metallicus.protonsdk.db.ChainProviderDao
import com.metallicus.protonsdk.db.ProtonDb
import com.metallicus.protonsdk.db.TokenContractDao
import com.metallicus.protonsdk.eosio.commander.GsonEosTypeAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ProtonModule {
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
	fun provideDb(context: Context): ProtonDb {
		return Room
			.databaseBuilder(context, ProtonDb::class.java, "protonsdk.db")
			.fallbackToDestructiveMigration()
			.build()
	}

	@Singleton
	@Provides
	fun provideChainProviderDao(db: ProtonDb): ChainProviderDao {
		return db.chainProviderDao()
	}

	@Singleton
	@Provides
	fun provideTokenContractDao(db: ProtonDb): TokenContractDao {
		return db.tokenContractDao()
	}

	@Singleton
	@Provides
	fun provideAccountDao(db: ProtonDb): AccountDao {
		return db.accountDao()
	}

	@Singleton
	@Provides
	fun provideProtonChainService(context: Context, gson: Gson): ProtonChainService {
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
			.baseUrl(context.getString(R.string.defaultProtonChainUrl))
			.addConverterFactory(GsonConverterFactory.create(gson))
			.addCallAdapterFactory(LiveDataCallAdapterFactory())
			.client(httpClient.build())
			.build()
			.create(ProtonChainService::class.java)
	}

	@Singleton
	@Provides
	fun providePrefs(context: Context): Prefs {
		return Prefs(context)
	}

	@Singleton
	@Provides
	fun provideSecureKeys(context: Context): SecureKeys {
		return SecureKeys(context)
	}
}