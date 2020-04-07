/*
 * Copyright (C) 2017 adorsys GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.proton.protonchain.securestorage;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.proton.protonchain.R;

import static android.content.Context.MODE_PRIVATE;
import static com.proton.protonchain.securestorage.SecureStorageException.ExceptionType.CRYPTO_EXCEPTION;
import static com.proton.protonchain.securestorage.SecureStorageProvider.context;

// Edited by joey-harward on 9/19/18

public final class SecurePreferences {
	private static String SharedPreferencesName = "SecurePreferences";

	// hidden constructor to disable initialization
	private SecurePreferences() {
	}

	public static String getSharedPreferencesName() {
		return SharedPreferencesName;
	}

	public static void setSharedPreferencesName(String sharedPreferencesName) {
		SharedPreferencesName = sharedPreferencesName;
	}

	public static void setValue(@NonNull String key,
								@NonNull String value,
								@Nullable String password) throws SecureStorageException {
		String transformedValue = "";
		if (password != null) {
			transformedValue = SecretKeyTool.encryptMessage(key, value, password);
		} else {
			if (!KeystoreTool.keyPairExists()) {
				KeystoreTool.generateKeyPair();
			}

			transformedValue = KeystoreTool.encryptMessage(value);
		}
		if (TextUtils.isEmpty(transformedValue)) {
			throw new SecureStorageException(context.get().getString(R.string.secure_storage_problem_encryption), null, CRYPTO_EXCEPTION);
		} else {
			setSecureValue(key, transformedValue);
		}
	}

	public static void setValue(@NonNull String key,
								@NonNull String value) throws SecureStorageException {
		setValue(key, value, null);
	}

	public static void setValue(@NonNull String key,
								boolean value,
								@Nullable String password) throws SecureStorageException {
		setValue(key, String.valueOf(value), password);
	}

	public static void setValue(@NonNull String key,
								boolean value) throws SecureStorageException {
		setValue(key, value, null);
	}

	public static void setValue(@NonNull String key,
								float value,
								@Nullable String password) throws SecureStorageException {
		setValue(key, String.valueOf(value), password);
	}

	public static void setValue(@NonNull String key,
								float value) throws SecureStorageException {
		setValue(key, value, null);
	}

	public static void setValue(@NonNull String key,
								long value,
								@Nullable String password) throws SecureStorageException {
		setValue(key, String.valueOf(value), password);
	}

	public static void setValue(@NonNull String key,
								long value) throws SecureStorageException {
		setValue(key, value, null);
	}

	public static void setValue(@NonNull String key,
								int value,
								@Nullable String password) throws SecureStorageException {
		setValue(key, String.valueOf(value), password);
	}

	public static void setValue(@NonNull String key,
								int value) throws SecureStorageException {
		setValue(key, value, null);
	}

	/*public static void setValue(@NonNull String key,
                                @NonNull Set<String> value) throws SecureStorageException {
        setValue(key + KEY_SET_COUNT_POSTFIX, String.valueOf(value.size()));

        int i = 0;
        for (String s : value) {
            setValue(key + "_" + (i++), s);
        }
    }*/

	@Nullable
	public static String getStringValue(@NonNull String key,
										@Nullable String password,
										@Nullable String defValue) {
		String secureValue = getSecureValue(key);
		try {
			if (!TextUtils.isEmpty(secureValue)) {
				if (password != null) {
					return SecretKeyTool.decryptMessage(key, secureValue, password);
				} else {
					return KeystoreTool.decryptMessage(secureValue);
				}
			} else {
				return defValue;
			}
		} catch (SecureStorageException e) {
			return defValue;
		}
	}

	@Nullable
	public static String getStringValue(@NonNull String key,
										@Nullable String defValue) {
		return getStringValue(key, null, defValue);
	}

	public static boolean getBooleanValue(@NonNull String key,
										  @Nullable String password,
										  boolean defValue) {
		return Boolean.parseBoolean(getStringValue(key, password, String.valueOf(defValue)));
	}

	public static boolean getBooleanValue(@NonNull String key, boolean defValue) {
		return getBooleanValue(key, null, defValue);
	}

	public static float getFloatValue(@NonNull String key, @Nullable String password, float defValue) {
		String stringVal = getStringValue(key, password, String.valueOf(defValue));
		if (stringVal != null) {
			return Float.parseFloat(stringVal);
		} else {
			return defValue;
		}
	}

	public static float getFloatValue(@NonNull String key, float defValue) {
		return getFloatValue(key, null, defValue);
	}

	public static long getLongValue(@NonNull String key, @Nullable String password, long defValue) {
		String stringVal = getStringValue(key, password, String.valueOf(defValue));
		if (stringVal != null) {
			return Long.parseLong(stringVal);
		} else {
			return defValue;
		}
	}

	public static long getLongValue(@NonNull String key, long defValue) {
		return getLongValue(key, null, defValue);
	}

	public static int getIntValue(@NonNull String key, @Nullable String password, int defValue) {
		String stringVal = getStringValue(key, password, String.valueOf(defValue));
		if (stringVal != null) {
			return Integer.parseInt(stringVal);
		} else {
			return defValue;
		}
	}

	public static int getIntValue(@NonNull String key, int defValue) {
		return getIntValue(key, null, defValue);
	}

	@NonNull
    /*public static Set<String> getStringSetValue(@NonNull String key,
                                                @NonNull Set<String> defValue) {
        int size = getIntValue(key + KEY_SET_COUNT_POSTFIX, -1);

        if (size == -1) {
            return defValue;
        }

        Set<String> res = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            res.add(getStringValue(key + "_" + i, ""));
        }

        return res;
    }*/

    public static SharedPreferences getSharedPreferences() {
		return context.get()
			.getSharedPreferences(getSharedPreferencesName(), MODE_PRIVATE);
	}

	public static boolean contains(@NonNull String key) {
		return getSharedPreferences().contains(key);
	}

	public static void removeValue(@NonNull String key) {
		removeSecureValue(key);
	}

	public static void clearAllValues() throws SecureStorageException {
		clearAllSecureValues();

//		if (KeystoreTool.keyPairExists()) {
			KeystoreTool.deleteKeyPair();
//		}
	}

	public static void registerOnSharedPreferenceChangeListener(@NonNull SharedPreferences.OnSharedPreferenceChangeListener listener) {
		getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
	}

	public static void unregisterOnSharedPreferenceChangeListener(@NonNull SharedPreferences.OnSharedPreferenceChangeListener listener) {
		getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
	}

	private static void setSecureValue(@NonNull String key,
									   @NonNull String value) {
		getSharedPreferences().edit().putString(key, value).apply();
	}

	@Nullable
	private static String getSecureValue(@NonNull String key) {
		return getSharedPreferences().getString(key, null);
	}

	private static void removeSecureValue(@NonNull String key) {
		getSharedPreferences().edit().remove(key).apply();
	}

	private static void clearAllSecureValues() {
		getSharedPreferences().edit().clear().apply();
	}
}