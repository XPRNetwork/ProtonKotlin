package com.proton.protonchain.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class UserNameBody(val account: String, val sign: String, val name: String)

interface ProtonChainService {
	@POST
	suspend fun updateUserName(
		@Url url: String,
		@Body body: UserNameBody): Response<JsonObject>

	@POST
	suspend fun uploadUserAvatar(
		@Url url: String,
		@Body body: MultipartBody): Response<JsonObject>
}