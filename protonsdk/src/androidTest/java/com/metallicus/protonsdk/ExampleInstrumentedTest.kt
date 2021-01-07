package com.metallicus.protonsdk

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
	@Test
	fun checkPackage() {
		// Context of the app under test.
		val appContext = InstrumentationRegistry.getInstrumentation().targetContext
		assertThat(appContext.packageName, containsString("com.metallicus.protonsdk"))
	}

	@Test
	fun checkAESDecrypt() {
		val cipherTextHex =
			"2a2049eee14c2bf2d73eae16d84affa1084e942f361ba846eb726ce56299e489d9d8afdcbd41bbca2fedc36c2ba5aef9373d757225e43654a2e98c99413c5f034f15bf7b9013b2dbe52951d2a4c537a5443a87914a5904571e7370a1c6d493cb9ce1a1d48dd80857a4d90bfe72b54ead940896192d6314d287606473734c17d75e988ce651e846286b9d2a06ecf100fd1832003948dd913e97cdde47cd6a1e9d06a1bd6397c55712721c627b9cc3c543f39cf6043da4241db23a6e7dfb14afe544da8bcffa26e078bf4a9580e54e044ab85b09ecabdc57ba9c5bc7008365be51"
		val keyHex = "728f6bb27af36663bc51f3f7786d132d9ebf52bc7a46a4090e63a91ad058895a"
		val ivHex = "4126bb121c0896cecf271af079c8c2d9"

		val cipherTextByteArray = cipherTextHex.hexStringToByteArray2()
		val keyByteArray = keyHex.hexStringToByteArray2()
		val ivByteArray = ivHex.hexStringToByteArray2()

		val decryptedByteArray: ByteArray = try {
			cipherTextByteArray.aesDecrypt(keyByteArray, ivByteArray)
		} catch (e: Exception) {
			Timber.e(e)
			byteArrayOf(0)
		}

		assert(decryptedByteArray.isNotEmpty())
	}

	private fun String.hexStringToByteArray(): ByteArray {
		val s = toUpperCase(Locale.ROOT)
		val len = s.length
		val result = ByteArray(len / 2)
		for (i in 0 until len step 2) {
			val firstIndex = indexOf(s[i])
			val secondIndex = indexOf(s[i + 1])

			val octet = firstIndex.shl(4).or(secondIndex)
			result[i.shr(1)] = octet.toByte()
		}
		return result
	}

	private fun String.hexStringToByteArray2(): ByteArray {
		val s = toUpperCase(Locale.ROOT)
		val len = s.length
		val data = ByteArray(len / 2)
		var i = 0
		while (i < len) {
			data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
			i += 2
		}
		return data
	}

	@Throws(
		NoSuchAlgorithmException::class,
		NoSuchPaddingException::class,
		InvalidKeyException::class,
		InvalidAlgorithmParameterException::class,
		IllegalBlockSizeException::class,
		BadPaddingException::class
	)
	private fun ByteArray.aesDecrypt(key: ByteArray, iv: ByteArray): ByteArray {
		val keySpec = SecretKeySpec(key, "AES")
		val ivSpec = IvParameterSpec(iv)
		val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
		return cipher.doFinal(this)
	}
}
