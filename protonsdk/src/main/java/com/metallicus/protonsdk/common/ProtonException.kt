package com.metallicus.protonsdk.common

import androidx.work.Data

class ProtonException : Exception {
	var code: Int = ProtonError.DEFAULT_ERROR

	constructor(message: String, code: Int) : super(message) { this.code = code }
	constructor(error: ProtonError) : super(error.message) { this.code = error.code }
	constructor(errorData: Data?) : this(ProtonError(errorData))
}