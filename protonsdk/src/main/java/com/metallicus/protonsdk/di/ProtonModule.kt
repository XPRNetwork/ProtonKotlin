/*
 * Copyright (c) 2020 Proton Chain LLC, Delaware
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
package com.metallicus.protonsdk.di

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.metallicus.protonsdk.R
import com.metallicus.protonsdk.api.ESRCallbackService
import com.metallicus.protonsdk.api.ProtonChainService
import com.metallicus.protonsdk.common.SecureKeys
import com.metallicus.protonsdk.common.Prefs
import com.metallicus.protonsdk.db.*
import com.metallicus.protonsdk.eosio.commander.GsonEosTypeAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ProtonModule {
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
	fun provideAccountContactDao(db: ProtonDb): AccountContactDao {
		return db.accountContactDao()
	}

	@Singleton
	@Provides
	fun provideCurrencyBalanceDao(db: ProtonDb): CurrencyBalanceDao {
		return db.currencyBalanceDao()
	}

	@Singleton
	@Provides
	fun provideActionDao(db: ProtonDb): ActionDao {
		return db.actionDao()
	}

	@Singleton
	@Provides
	fun provideProtonChainService(context: Context): ProtonChainService {
//		val logging = HttpLoggingInterceptor()
//		logging.level = HttpLoggingInterceptor.Level.BODY

		val httpClient = OkHttpClient.Builder()
			.callTimeout(30, TimeUnit.SECONDS)
			.connectTimeout(30, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(30, TimeUnit.SECONDS)
//			.addInterceptor(logging)

		val gson = GsonBuilder()
			.registerTypeAdapterFactory(GsonEosTypeAdapterFactory())
			.serializeNulls()
//			.excludeFieldsWithoutExposeAnnotation()
			.create()

		return Retrofit.Builder()
			.baseUrl(context.getString(R.string.defaultProtonChainUrl))
			.addConverterFactory(GsonConverterFactory.create(gson))
			.client(httpClient.build())
			.build()
			.create(ProtonChainService::class.java)
	}

	@Singleton
	@Provides
	fun provideESRCallbackService(context: Context): ESRCallbackService {
		val logging = HttpLoggingInterceptor()
		logging.level = HttpLoggingInterceptor.Level.BODY

		val httpClient = OkHttpClient.Builder()
			.callTimeout(30, TimeUnit.SECONDS)
			.connectTimeout(30, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS)
			.writeTimeout(30, TimeUnit.SECONDS)
			.addInterceptor(logging)

		val gson = GsonBuilder()
			.setLenient()
			.serializeNulls()
			.create()

		return Retrofit.Builder()
			.baseUrl(context.getString(R.string.defaultESRCallbackUrl))
			.addConverterFactory(GsonConverterFactory.create(gson))
			.client(httpClient.build())
			.build()
			.create(ESRCallbackService::class.java)
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