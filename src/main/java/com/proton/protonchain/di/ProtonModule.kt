package com.proton.protonchain.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.proton.protonchain.R
import com.proton.protonchain.api.LiveDataCallAdapterFactory
import com.proton.protonchain.api.ProtonChainService
import com.proton.protonchain.common.Prefs
import com.proton.protonchain.db.AccountDao
import com.proton.protonchain.db.ChainProviderDao
import com.proton.protonchain.db.ProtonChainDb
import com.proton.protonchain.db.TokenContractDao
import com.proton.protonchain.eosio.commander.GsonEosTypeAdapterFactory
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
	fun provideDb(context: Context): ProtonChainDb {
		return Room
			.databaseBuilder(context, ProtonChainDb::class.java, "protonchain.db")
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
	fun provideTokenContractDao(db: ProtonChainDb): TokenContractDao {
		return db.tokenContractDao()
	}

	@Singleton
	@Provides
	fun provideAccountDao(db: ProtonChainDb): AccountDao {
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
}