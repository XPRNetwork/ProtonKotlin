package com.proton.protonchain.model

/**
 * A generic class that a loading status, data, and optional message.
 */
data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
	companion object {
		fun <T> success(data: T?): Resource<T> {
			return Resource(Status.SUCCESS, data, null)
		}

		fun <T> error(msg: String, data: T?): Resource<T> {
			return Resource(Status.ERROR, data, msg)
		}

		fun <T> error(msg: String): Resource<T> {
			return error(msg, null)
		}

		fun <T> loading(data: T?): Resource<T> {
			return Resource(Status.LOADING, data, null)
		}

		fun <T> loading(): Resource<T> {
			return loading(null)
		}
	}
}
