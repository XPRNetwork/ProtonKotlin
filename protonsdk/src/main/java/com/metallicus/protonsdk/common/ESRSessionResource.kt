package com.metallicus.protonsdk.common

import com.metallicus.protonsdk.model.ESRSession

/**
 * A generic class that emulates ESR Session states with optional messages.
 */
data class ESRSessionResource(val status: ESRSessionStatus, val esrSession: ESRSession, val message: String?, val code: Int?) {
	companion object {
		fun onConnected(esrSession: ESRSession, message: String, code: Int): ESRSessionResource {
			return ESRSessionResource(
				ESRSessionStatus.CONNECTED,
				esrSession,
				message,
				code
			)
		}

		fun onMessage(esrSession: ESRSession, message: String): ESRSessionResource {
			return ESRSessionResource(
				ESRSessionStatus.MESSAGE,
				esrSession,
				message,
				null
			)
		}

		fun onClosing(esrSession: ESRSession, reason: String, code: Int): ESRSessionResource {
			return ESRSessionResource(
				ESRSessionStatus.CLOSING,
				esrSession,
				reason,
				code
			)
		}

		fun onClosed(esrSession: ESRSession, reason: String, code: Int): ESRSessionResource {
			return ESRSessionResource(
				ESRSessionStatus.CLOSED,
				esrSession,
				reason,
				code
			)
		}

		fun onFailure(esrSession: ESRSession, message: String, code: Int): ESRSessionResource {
			return ESRSessionResource(
				ESRSessionStatus.FAILURE,
				esrSession,
				message,
				code
			)
		}
	}
}
