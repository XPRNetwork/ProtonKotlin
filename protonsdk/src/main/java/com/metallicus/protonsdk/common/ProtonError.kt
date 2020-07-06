package com.metallicus.protonsdk.common

import androidx.work.Data

class ProtonError(
	var message: String,
	var code: Int = DEFAULT_ERROR
) {
	companion object {
		const val ERROR_MESSAGE_KEY = "errorMessage"
		const val ERROR_CODE_KEY = "errorCode"

		const val NO_ERROR = 0
		const val DEFAULT_ERROR = -1
		const val ACCOUNT_NOT_FOUND = -2
	}

	constructor(errorData: Data?) : this(
		errorData?.getString(ERROR_MESSAGE_KEY).orEmpty(),
		errorData?.getInt(ERROR_CODE_KEY, DEFAULT_ERROR) ?: DEFAULT_ERROR)
}
