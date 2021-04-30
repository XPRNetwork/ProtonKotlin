import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
	id(BuildPlugins.androidLibrary)
	kotlin(BuildPlugins.kotlinAndroid)
	kotlin(BuildPlugins.kotlinKapt)
	id(BuildPlugins.mavenPublish)
	id(BuildPlugins.dokka)
}

android {
	compileSdkVersion(Android.compileSdk)
	buildToolsVersion(Android.buildTools)

	defaultConfig {
		//applicationId = "com.metallicus.protonsdk"
		minSdkVersion(Android.minSdk)
		targetSdkVersion(Android.targetSdk)
		versionCode = ProtonSdk.versionCode
		versionName = ProtonSdk.versionName
		testInstrumentationRunner = TestLibraries.testRunner
		consumerProguardFiles(Android.Progaurd.consumeFile)
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}

	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_1_8.toString()
	}

	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile(Android.Progaurd.optimizeFile), Android.Progaurd.rulesFile)
		}
	}
}

dependencies {
	implementation(Libraries.kotlinStdLib)

	// Unit Testing
	testImplementation(TestLibraries.junit)
	androidTestImplementation(TestLibraries.testExt)
	androidTestImplementation(TestLibraries.espresso)

	// KTX
	implementation(Libraries.ktxCore)

	// Lifecycle
	implementation(Libraries.lifeCycleLiveData)

	// Room
	implementation(Libraries.roomKtx)
	kapt(Libraries.roomCompiler)

	// WorkManager
	implementation(Libraries.workManagerKtx)

	// OkHttp
	implementation(Libraries.okhttp3)
	implementation(Libraries.okhttp3Logging)

	// Retrofit
	implementation(Libraries.retrofit)
	implementation(Libraries.retrofitGsonConverter)

	// Dagger
	implementation(Libraries.dagger)
	kapt(Libraries.daggerCompiler)

	// Assisted Inject (Dagger)
	compileOnly(Libraries.daggerAssistedInjectAnnotations)
	kapt(Libraries.daggerAssistedInjectProcessor)

	// Coroutines
	implementation(Libraries.coroutinesCore)
	implementation(Libraries.coroutinesAndroid)

	// Timber
	implementation(Libraries.timber)

	// Gson
	implementation(Libraries.gson)

	// Guava
	implementation(Libraries.guava)

	// Greymass ESR
	implementation(Libraries.greymassESR)
}

// Dokka
tasks {
	// Dokka Task
	val dokka by getting(DokkaTask::class) {
		outputFormat = "html"
		outputDirectory = "$buildDir/dokka"

		configuration {
			perPackageOption {
				prefix = "com.metallicus.protonsdk.api"
				suppress = true
			}
			perPackageOption {
				prefix = "com.metallicus.protonsdk.db"
				suppress = true
			}
			perPackageOption {
				prefix = "com.metallicus.protonsdk.di"
				suppress = true
			}
			perPackageOption {
				prefix = "com.metallicus.protonsdk.repository"
				suppress = true
			}
			perPackageOption {
				prefix = "com.metallicus.protonsdk.worker"
				suppress = true
			}
		}
	}

	// publishing depends on build
	withType<PublishToMavenLocal> {
		dependsOn(build)
	}

//	// uploading to Bintray depends on publications
//	withType<BintrayUploadTask> {
//		dependsOn(publishToMavenLocal)
//	}
}

val sourcesJar by tasks.creating(Jar::class) {
	archiveClassifier.set("sources")
	from(android.sourceSets["main"].java.srcDirs)
}

afterEvaluate {
	publishing {
		repositories {
			maven {
				name = "GithubPackages"
				url = uri("https://maven.pkg.github.com/ProtonProtocol/ProtonKotlin")
				credentials {
					username = gradleLocalProperties(rootDir).getProperty("github.username")
					password = gradleLocalProperties(rootDir).getProperty("github.token")
				}
			}
			maven {
				name = "LocalTest"
				url = uri("$buildDir/repo")
			}
		}

		publications {
			create<MavenPublication>("protonsdk") {
				groupId = Publishing.groupId
				artifactId = Publishing.artifactId
				version = Publishing.version

				artifact("$buildDir/outputs/aar/protonsdk-release.aar")
				artifact(sourcesJar)

				pom.withXml {
					val dependenciesNode = asNode().appendNode("dependencies")
					configurations.implementation.get().allDependencies.forEach {
						if (it.group != null && it.version != null &&
							it.name != "unspecified" && it.version != "unspecified") {
							dependenciesNode.appendNode("dependency").apply {
								appendNode("groupId", it.group)
								appendNode("artifactId", it.name)
								appendNode("version", it.version)
							}
						}
					}
				}
			}
		}
	}
}