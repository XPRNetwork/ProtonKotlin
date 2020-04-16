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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;

import com.proton.protonchain.R;

import static android.content.Context.MODE_PRIVATE;
import static com.proton.protonchain.securestorage.SecureStorageException.ExceptionType.CRYPTO_EXCEPTION;

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

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								@NonNull String value,
								@Nullable String password) throws SecureStorageException {
		Context applicationContext = context.getApplicationContext();

		String transformedValue = "";
		if (password != null) {
			transformedValue = SecretKeyTool.encryptMessage(key, value, password);
		} else {
			if (!KeystoreTool.keyPairExists()) {
				KeystoreTool.generateKeyPair(context);
			}

			transformedValue = KeystoreTool.encryptMessage(applicationContext, value);
		}
		if (TextUtils.isEmpty(transformedValue)) {
			throw new SecureStorageException(context.getString(R.string.secureStorageEncryptionError), null, CRYPTO_EXCEPTION);
		} else {
			setSecureValue(context, key, transformedValue);
		}
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								@NonNull String value) throws SecureStorageException {
		setValue(context, key, value, null);
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								boolean value,
								@Nullable String password) throws SecureStorageException {
		setValue(context, key, String.valueOf(value), password);
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								boolean value) throws SecureStorageException {
		setValue(context, key, value, null);
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								float value,
								@Nullable String password) throws SecureStorageException {
		setValue(context, key, String.valueOf(value), password);
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								float value) throws SecureStorageException {
		setValue(context, key, value, null);
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								long value,
								@Nullable String password) throws SecureStorageException {
		setValue(context, key, String.valueOf(value), password);
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								long value) throws SecureStorageException {
		setValue(context, key, value, null);
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								int value,
								@Nullable String password) throws SecureStorageException {
		setValue(context, key, String.valueOf(value), password);
	}

	public static void setValue(@NonNull Context context,
								@NonNull String key,
								int value) throws SecureStorageException {
		setValue(context, key, value, null);
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
	public static String getStringValue(@NonNull Context context,
										@NonNull String key,
										@Nullable String password,
										@Nullable String defValue) {
		String secureValue = getSecureValue(context, key);
		try {
			if (!TextUtils.isEmpty(secureValue)) {
				if (password != null) {
					return SecretKeyTool.decryptMessage(key, secureValue, password);
				} else {
					return KeystoreTool.decryptMessage(context, secureValue);
				}
			} else {
				return defValue;
			}
		} catch (SecureStorageException e) {
			return defValue;
		}
	}

	@Nullable
	public static String getStringValue(@NonNull Context context,
										@NonNull String key,
										@Nullable String defValue) {
		return getStringValue(context, key, null, defValue);
	}

	public static boolean getBooleanValue(@NonNull Context context,
										  @NonNull String key,
										  @Nullable String password,
										  boolean defValue) {
		return Boolean.parseBoolean(getStringValue(context, key, password, String.valueOf(defValue)));
	}

	public static boolean getBooleanValue(@NonNull Context context,
										  @NonNull String key,
										  boolean defValue) {
		return getBooleanValue(context, key, null, defValue);
	}

	public static float getFloatValue(@NonNull Context context,
									  @NonNull String key,
									  @Nullable String password, float defValue) {
		String stringVal = getStringValue(context, key, password, String.valueOf(defValue));
		if (stringVal != null) {
			return Float.parseFloat(stringVal);
		} else {
			return defValue;
		}
	}

	public static float getFloatValue(@NonNull Context context,
									  @NonNull String key,
									  float defValue) {
		return getFloatValue(context, key, null, defValue);
	}

	public static long getLongValue(@NonNull Context context,
									@NonNull String key,
									@Nullable String password, long defValue) {
		String stringVal = getStringValue(context, key, password, String.valueOf(defValue));
		if (stringVal != null) {
			return Long.parseLong(stringVal);
		} else {
			return defValue;
		}
	}

	public static long getLongValue(@NonNull Context context,
									@NonNull String key,
									long defValue) {
		return getLongValue(context, key, null, defValue);
	}

	public static int getIntValue(@NonNull Context context,
								  @NonNull String key,
								  @Nullable String password, int defValue) {
		String stringVal = getStringValue(context, key, password, String.valueOf(defValue));
		if (stringVal != null) {
			return Integer.parseInt(stringVal);
		} else {
			return defValue;
		}
	}

	public static int getIntValue(@NonNull Context context,
								  @NonNull String key,
								  int defValue) {
		return getIntValue(context, key, null, defValue);
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

	public static SharedPreferences getSharedPreferences(@NonNull Context context) {
		return context.getSharedPreferences(getSharedPreferencesName(), MODE_PRIVATE);
	}

	public static boolean contains(@NonNull Context context,
								   @NonNull String key) {
		return getSharedPreferences(context).contains(key);
	}

	public static void removeValue(@NonNull Context context,
								   @NonNull String key) {
		removeSecureValue(context, key);
	}

	public static void clearAllValues(@NonNull Context context) throws SecureStorageException {
		clearAllSecureValues(context);

//		if (KeystoreTool.keyPairExists()) {
		KeystoreTool.deleteKeyPair();
//		}
	}

	public static void registerOnSharedPreferenceChangeListener(@NonNull Context context,
																@NonNull SharedPreferences.OnSharedPreferenceChangeListener listener) {
		getSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
	}

	public static void unregisterOnSharedPreferenceChangeListener(@NonNull Context context,
																  @NonNull SharedPreferences.OnSharedPreferenceChangeListener listener) {
		getSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
	}

	private static void setSecureValue(@NonNull Context context,
									   @NonNull String key,
									   @NonNull String value) {
		getSharedPreferences(context).edit().putString(key, value).apply();
	}

	@Nullable
	private static String getSecureValue(@NonNull Context context,
										 @NonNull String key) {
		return getSharedPreferences(context).getString(key, null);
	}

	private static void removeSecureValue(@NonNull Context context,
										  @NonNull String key) {
		getSharedPreferences(context).edit().remove(key).apply();
	}

	private static void clearAllSecureValues(@NonNull Context context) {
		getSharedPreferences(context).edit().clear().apply();
	}
}