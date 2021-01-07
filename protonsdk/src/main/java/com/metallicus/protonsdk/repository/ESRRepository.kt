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
package com.metallicus.protonsdk.repository

import com.metallicus.protonsdk.api.*
import com.metallicus.protonsdk.db.ESRSessionDao
import com.metallicus.protonsdk.model.ESRSession
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ESRRepository @Inject constructor(
	private val esrCallbackService: ESRCallbackService,
	private val esrSessionDao: ESRSessionDao
) {
	suspend fun cancelAuthorizeESR(url: String, error: String): Response<String> {
		return esrCallbackService.cancelAuthorizeESR(url, CancelAuthorizeESRBody(error))
	}

	suspend fun authorizeESR(url: String, params: Map<String, String>): Response<String> {
		return esrCallbackService.authorizeESR(url, params)
	}

	suspend fun getESRSession(id: String): ESRSession {
		return esrSessionDao.findById(id)
	}

	suspend fun getESRSessions(): List<ESRSession> {
		return esrSessionDao.findAll()
	}

	suspend fun addESRSession(esrSession: ESRSession) {
		esrSessionDao.insert(esrSession)
	}

	suspend fun updateESRSession(esrSession: ESRSession) {
		esrSessionDao.update(esrSession)
	}

	suspend fun removeESRSession(esrSession: ESRSession) {
		esrSessionDao.remove(esrSession.id)
	}

	suspend fun removeAllESRSessions() {
		esrSessionDao.removeAll()
	}
}
