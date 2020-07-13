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
package com.metallicus.protonsdk.common

import androidx.work.Data

class ProtonError(
	var message: String,
	var code: Int = DEFAULT_ERROR
) {
	companion object {
		const val ERROR_MESSAGE_KEY = "errorMessage"
		const val ERROR_CODE_KEY = "errorCode"

		// Error Codes
		const val NO_ERROR = 0
		const val DEFAULT_ERROR = -1
		const val ACCOUNT_NOT_FOUND = -2
	}

	constructor(errorData: Data?) : this(
		errorData?.getString(ERROR_MESSAGE_KEY).orEmpty(),
		errorData?.getInt(ERROR_CODE_KEY, DEFAULT_ERROR) ?: DEFAULT_ERROR)
}
