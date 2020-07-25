import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.dokka.gradle.DokkaTask
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask

plugins {
	id(BuildPlugins.androidLibrary)
	kotlin(BuildPlugins.kotlinAndroid)
	kotlin(BuildPlugins.kotlinAndroidExtensions)
	kotlin(BuildPlugins.kotlinKapt)
	id(BuildPlugins.mavenPublish)
	id(BuildPlugins.dokka)
	id(BuildPlugins.bintray)
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
	implementation(Libraries.daggerAndroidSupport)
	kapt(Libraries.daggerAndroidProcessor)
	kapt(Libraries.daggerAndroidCompiler)

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
	//implementation "com.greymass:esrsdk:1.0.1"
}

// Dokka
tasks {
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

	withType<PublishToMavenLocal> {
		dependsOn(assemble)
	}

	withType<BintrayUploadTask> {
		dependsOn(publishToMavenLocal)
	}
}

group = Publishing.publishedGroupId
version = Publishing.libraryVersion

val sourcesJar by tasks.creating(Jar::class) {
	archiveClassifier.set("sources")
	from(android.sourceSets["main"].java.srcDirs)
}

afterEvaluate {
	publishing {
		publications {
			create<MavenPublication>(Publishing.Publications.debug) {
				groupId = "com.metallicus"
				artifactId = "${Publishing.libraryName}-debug"
				version = Publishing.libraryVersion

				//artifact(buildOutputs["debug"].outputFile)
				artifact("$buildDir/outputs/aar/protonsdk-debug.aar")
				artifact(sourcesJar)

				pom {
					name.set(Publishing.libraryName)
					description.set(Publishing.libraryDescription)
					url.set(Publishing.siteUrl)

					licenses {
						license {
							name.set(Publishing.licenseName)
							url.set(Publishing.licenseUrl)
						}
					}

					developers {
						developer {
							id.set(Publishing.developerId)
							name.set(Publishing.developerName)
							email.set(Publishing.developerEmail)
						}
					}

					scm {
						connection.set(Publishing.gitUrl)
						developerConnection.set(Publishing.gitUrl)
						url.set(Publishing.siteUrl)
					}

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

			create<MavenPublication>(Publishing.Publications.release) {
				groupId = "com.metallicus"
				artifactId = Publishing.libraryName
				version = Publishing.libraryVersion

				//artifact(buildOutputs["release"].outputFile)
				artifact("$buildDir/outputs/aar/protonsdk-release.aar")
				artifact(sourcesJar)

				pom {
					name.set(Publishing.libraryName)
					description.set(Publishing.libraryDescription)
					url.set(Publishing.siteUrl)

					licenses {
						license {
							name.set(Publishing.licenseName)
							url.set(Publishing.licenseUrl)
						}
					}

					developers {
						developer {
							id.set(Publishing.developerId)
							name.set(Publishing.developerName)
							email.set(Publishing.developerEmail)
						}
					}

					scm {
						connection.set(Publishing.gitUrl)
						developerConnection.set(Publishing.gitUrl)
						url.set(Publishing.siteUrl)
					}

					pom.withXml {
						val dependenciesNode = asNode().appendNode("dependencies")
						configurations.implementation.get().allDependencies.forEach {
							if (it.group != null && it.version != null &&
								it.name != "unspecified" && it.version != "unspecified"
							) {
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

	bintray {
		user = gradleLocalProperties(rootDir).getProperty("bintray.user")
		key = gradleLocalProperties(rootDir).getProperty("bintray.apikey")
		publish = true

		setPublications(Publishing.Publications.release)

		pkg.apply {
			repo = Publishing.bintrayRepo
			name = Publishing.bintrayName
			userOrg = Publishing.userOrganization
			desc = Publishing.libraryDescription
			websiteUrl = Publishing.siteUrl
			vcsUrl = Publishing.gitUrl
			setLicenses(Publishing.allLicenses)
			publish = true
			publicDownloadNumbers = true

			version.apply {
				name = Publishing.libraryVersion
				desc = Publishing.libraryDescription
//				gpg.sign = true
//				gpg.passphrase = gradleLocalProperties(rootDir).getProperty("bintray.gpg.password")
			}
		}
	}
}