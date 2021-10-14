/*
 * Copyright (c) 2021 Proton Chain LLC, Delaware
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
const val kotlinVersion = "1.5.31"
const val orchidVersion = "0.21.1"

object ProtonSdk {
	const val versionCode = 47
	const val versionName = "1.0.9"
}

object BuildPlugins {
	object Versions {
		const val gradle = "4.2.2"
		const val dokka = "0.10.1" // TODO: 1.4.0
	}

	const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.gradle}"
	const val androidLibrary = "com.android.library"
	const val kotlinAndroid = "android"
	const val kotlinKapt = "kapt"
	const val mavenPublish = "maven-publish"
	const val dokka = "org.jetbrains.dokka"
	const val orchid = "com.eden.orchidPlugin"
}

object Android {
	const val minSdk = 21
	const val compileSdk = 31
	const val targetSdk = compileSdk
	const val buildTools = "30.0.3"

	object Progaurd {
		const val consumeFile = "consumer-rules.pro"
		const val optimizeFile = "proguard-android-optimize.txt"
		const val rulesFile = "proguard-rules.pro"
	}
}

object Libraries {
	private object Versions {
		const val ktx = "1.6.0-rc01"
		const val lifecycleLiveData = "2.3.1"
		const val room = "2.4.0-alpha04"
		const val workManager = "2.6.0"
		const val okhttp3 = "5.0.0-alpha.2"
		const val retrofit = "2.9.0"
		const val dagger = "2.38.1"
		const val daggerAssistedInject = "0.6.0"
		const val coroutines = "1.5.2"
		const val timber = "4.7.1"
		const val gson = "2.8.8"
		const val guava = "30.1.1-jre"
		const val esr = "1.0.1"
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
	const val greymassESR = "com.metallicus:esrsdk:${Versions.esr}"
}

object TestLibraries {
	private object Versions {
		const val junit = "4.13.2"
		const val testExt = "1.1.2"
		const val espresso = "3.3.0"
	}
	const val junit = "junit:junit:${Versions.junit}"
	const val testExt = "androidx.test.ext:junit:${Versions.testExt}"
	const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
	const val testRunner = "androidx.test.runner.AndroidJUnitRunner"
}

object Publishing {
	const val groupId = "com.metallicus"
	const val artifactId = "protonsdk"
	const val version = ProtonSdk.versionName
}