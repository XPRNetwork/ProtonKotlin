package com.metallicus.protonsdk.common

/**
 * A generic class that a loading status, data, and optional message.
 */
data class Resource<out T>(val status: Status, val data: T?, val message: String?, val code: Int?) {
	companion object {
		fun <T> success(data: T?): Resource<T> {
			return Resource(
				Status.SUCCESS,
				data,
				null,
				ProtonError.NO_ERROR
			)
		}

		fun <T> error(msg: String): Resource<T> {
			return Resource(
				Status.ERROR,
				null,
				msg,
				ProtonError.DEFAULT_ERROR
			)
		}

		fun <T> error(msg: String, code: Int): Resource<T> {
			return Resource(
				Status.ERROR,
				null,
				msg,
				code
			)
		}

		fun <T> error(error: ProtonError): Resource<T> {
			return Resource(
				Status.ERROR,
				null,
				error.message,
				error.code
			)
		}

		fun <T> error(exception: ProtonException): Resource<T> {
			return Resource(
				Status.ERROR,
				null,
				exception.message,
				exception.code
			)
		}

		fun <T> loading(): Resource<T> {
			return Resource(
				Status.LOADING,
				null,
				null,
				ProtonError.DEFAULT_ERROR
			)
		}
	}
}
