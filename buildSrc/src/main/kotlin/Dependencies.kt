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
const val kotlinVersion = "1.4.32"
const val orchidVersion = "0.21.1"

object ProtonSdk {
	const val versionCode = 37
	const val versionName = "0.9.4"
}

object BuildPlugins {
	object Versions {
		const val gradle = "4.1.3"
		const val dokka = "0.10.1" // TODO: 1.4.0
		const val bintray = "1.8.5"
	}

	const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.gradle}"
	const val androidLibrary = "com.android.library"
	const val kotlinAndroid = "android"
	const val kotlinKapt = "kapt"
	const val mavenPublish = "maven-publish"
	const val dokka = "org.jetbrains.dokka"
	const val orchid = "com.eden.orchidPlugin"
	const val bintray = "com.jfrog.bintray"
}

object Android {
	const val minSdk = 21
	const val compileSdk = 30
	const val targetSdk = compileSdk
	const val buildTools = "31.0.0-rc02"

	object Progaurd {
		const val consumeFile = "consumer-rules.pro"
		const val optimizeFile = "proguard-android-optimize.txt"
		const val rulesFile = "proguard-rules.pro"
	}
}

object Libraries {
	private object Versions {
		const val ktx = "1.5.0-beta03"
		const val lifecycleLiveData = "2.3.1"
		const val room = "2.3.0-rc01"
		const val workManager = "2.5.0"
		const val okhttp3 = "5.0.0-alpha.2"
		const val retrofit = "2.9.0"
		const val dagger = "2.29.1"
		const val daggerAssistedInject = "0.6.0"
		const val coroutines = "1.4.3"
		const val timber = "4.7.1"
		const val gson = "2.8.6"
		const val guava = "30.1.1-jre"
		const val esr = "1.0.6"
	}

	const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
	const val ktxCore = "androidx.core:core-ktx:${Versions.ktx}"
	const val lifeCycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycleLiveData}"
	const val roomKtx = "androidx.room:room-ktx:${Versions.room}"
	const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
	const val workManagerKtx = "androidx.work:work-runtime-ktx:${Versions.workManager}"
	const val okhttp3 = "com.squareup.okhttp3:okhttp:${Versions.okhttp3}"
	const val okhttp3Logging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp3}"
	const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
	const val retrofitGsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
	const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
	const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
	const val daggerAssistedInjectAnnotations = "com.squareup.inject:assisted-inject-annotations-dagger2:${Versions.daggerAssistedInject}"
	const val daggerAssistedInjectProcessor = "com.squareup.inject:assisted-inject-processor-dagger2:${Versions.daggerAssistedInject}"
	const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
	const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
	const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
	const val gson = "com.google.code.gson:gson:${Versions.gson}"
	const val guava = "com.google.guava:guava:${Versions.guava}"
	const val orchidDocs = "io.github.javaeden.orchid:OrchidDocs:$orchidVersion"
	const val orchidKotlindoc = "io.github.javaeden.orchid:OrchidKotlindoc:$orchidVersion"
	const val orchidPluginDocs = "io.github.javaeden.orchid:OrchidPluginDocs:$orchidVersion"
	const val orchidGithub = "io.github.javaeden.orchid:OrchidGithub:$orchidVersion"
	const val greymassESR = "com.greymass:esrsdk:${Versions.esr}"
}

object TestLibraries {
	private object Versions {
		const val junit = "4.13.1"
		const val testExt = "1.1.2"
		const val espresso = "3.3.0"
	}
	const val junit = "junit:junit:${Versions.junit}"
	const val testExt = "androidx.test.ext:junit:${Versions.testExt}"
	const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
	const val testRunner = "androidx.test.runner.AndroidJUnitRunner"
}

object Publishing {
	object Publications {
		const val debug = "ProtonSDKDebug"
		const val release = "ProtonSDKRelease"
	}
	const val bintrayRepo = "ProtonKotlin"
	const val bintrayName = "com.metallicus.protonsdk"
	const val userOrganization = "protonprotocol"

	const val libraryName = "protonsdk"
	const val libraryVersion = ProtonSdk.versionName

	const val publishedGroupId = "com.metallicus"

	const val libraryDescription = "Kotlin library for handling Proton Chain operations"
	const val siteUrl = "https://github.com/ProtonProtocol/ProtonKotlin"
	const val gitUrl = "https://github.com/ProtonProtocol/ProtonKotlin.git"
	const val developerId = "joey-harward"
	const val developerName = "Metallicus Inc."
	const val developerEmail = "joey@metalpay.co"
	const val licenseName = "MIT License"
	const val licenseUrl = "https://opensource.org/licenses/MIT"
	const val allLicenses = "MIT"
}