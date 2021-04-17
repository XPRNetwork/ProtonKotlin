/*
 * Copyright (C) 2021 Proton Chain LLC, Delaware
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
package com.metallicus.protonsdk.securestorage;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.metallicus.protonsdk.securestorage.SecureStorageException.ExceptionType.CRYPTO_EXCEPTION;
import static com.metallicus.protonsdk.securestorage.SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION;

final class SecretKeyTool {
	private static final String KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final String KEY_ENCRYPTION_ALGORITHM = "AES";
	private static final String KEY_CHARSET = "UTF-8";
	private static final String KEY_TRANSFORMATION_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int KEY_ITERATION_COUNT = 100;
	private static final int KEY_LENGTH = 256;

	// hidden constructor to disable initialization
	private SecretKeyTool() {
	}

	@Nullable
	static String encryptMessage(@NonNull String key, @NonNull String plainMessage, @NonNull String password) throws SecureStorageException {
		try {
			byte[] salt = key.getBytes();
			byte[] iv = key.substring(0, 16).getBytes();

			SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, KEY_ITERATION_COUNT, KEY_LENGTH);

			Key secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), KEY_ENCRYPTION_ALGORITHM);

			Cipher cipher = Cipher.getInstance(KEY_TRANSFORMATION_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CipherOutputStream cipherOutputStream = new CipherOutputStream(
				outputStream, cipher);
			cipherOutputStream.write(plainMessage.getBytes(KEY_CHARSET));
			cipherOutputStream.close();

			byte[] values = outputStream.toByteArray();
			return Base64.encodeToString(values, Base64.DEFAULT);

		} catch (Exception e) {
			throw new SecureStorageException(e.getMessage(), e, KEYSTORE_EXCEPTION);
		}
	}

	@NonNull
	static String decryptMessage(@NonNull String key, @NonNull String encryptedMessage, @NonNull String password) throws SecureStorageException {
		try {
			byte[] salt = key.getBytes();
			byte[] iv = key.substring(0, 16).getBytes();

			SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, KEY_ITERATION_COUNT, KEY_LENGTH);

			Key secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), KEY_ENCRYPTION_ALGORITHM);

			Cipher cipher = Cipher.getInstance(KEY_TRANSFORMATION_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

			CipherInputStream cipherInputStream = new CipherInputStream(
				new ByteArrayInputStream(Base64.decode(encryptedMessage, Base64.DEFAULT)), cipher);

			List<Byte> values = new ArrayList<>();

			int nextByte;
			while ((nextByte = cipherInputStream.read()) != -1) { //NOPMD
				values.add((byte) nextByte);
			}

			byte[] bytes = new byte[values.size()];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = values.get(i);
			}

			return new String(bytes, 0, bytes.length, KEY_CHARSET);

		} catch (Exception e) {
			throw new SecureStorageException(e.getMessage(), e, CRYPTO_EXCEPTION);
		}
	}
}