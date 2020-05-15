package com.proton.protonchain.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

data class UserNameBody(val account: String, val sign: String, val name: String)

interface ProtonChainService {
	@GET
	suspend fun getChainProviders(
		@Url url: String): Response<JsonObject>

	@POST
	suspend fun updateUserName(
		@Url url: String,
		@Body body: UserNameBody): Response<JsonObject>

	@POST
	suspend fun uploadUserAvatar(
		@Url url: String,
		@Body body: MultipartBody): Response<JsonObject>
}