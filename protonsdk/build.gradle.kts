import org.jetbrains.dokka.gradle.DokkaTask

plugins {
	id(BuildPlugins.androidLibrary)
	kotlin(BuildPlugins.kotlinAndroid)
	kotlin(BuildPlugins.kotlinAndroidExtensions)
	kotlin(BuildPlugins.kotlinKapt)
	id(BuildPlugins.mavenPublish)
	id(BuildPlugins.dokka)

	//apply plugin: 'com.jfrog.bintray'
	//apply plugin: 'com.github.dcendents.android-maven'
}

android {
	compileSdkVersion(AndroidSdk.compile)
	buildToolsVersion("30.0.1")

	defaultConfig {
		//applicationId = "com.gradle.kotlindsl"
		minSdkVersion(AndroidSdk.min)
		targetSdkVersion(AndroidSdk.target)
		versionCode = AndroidSdk.versionCode
		versionName = AndroidSdk.versionName
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
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
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
}

//ext {
//	bintrayRepo = "ProtonKotlin"
//	bintrayName = "com.metallicus.protonsdk"
//	userOrganization = "protonprotocol"
//
//	libraryName = "protonsdk"
//
//	publishedGroupId = "com.metallicus"
//	artifact = "protonsdk"
//	libraryVersion = android.defaultConfig.versionName
//
//	libraryDescription = "Kotlin library for handling Proton Chain operations"
//	siteUrl = "https://github.com/ProtonProtocol/ProtonKotlin"
//	gitUrl = "https://github.com/ProtonProtocol/ProtonKotlin.git"
//	developerId = "joey-harward"
//	developerName = "Metallicus Inc."
//	developerEmail = "joey@metalpay.co"
//	licenseName = "MIT License"
//	licenseUrl = "https://opensource.org/licenses/MIT"
//	allLicenses = ["MIT"]
//}
//
//group = publishedGroupId
//version = libraryVersion
//
//task sourcesJar(type: Jar) {
//	archiveClassifier.set('sources')
//	from android.sourceSets.main.java.srcDirs
//}
//
//def pomConfig = {
//	licenses {
//		license {
//			name licenseName
//			url licenseUrl
//		}
//	}
//	developers {
//		developer {
//			id developerId
//			name developerName
//			email developerEmail
//		}
//	}
//	scm {
//		connection gitUrl
//		developerConnection gitUrl
//		url siteUrl
//	}
//}
//
//project.afterEvaluate {
//	publishing {
//		publications {
//			ProtonSDKDebug(MavenPublication) {
//				groupId = 'com.metallicus'
//				artifactId = "${project.getName()}-debug"
//				version = android.defaultConfig.versionName
//
//				artifact bundleDebugAar
//
//				pom.withXml {
//					def root = asNode()
//					root.appendNode('name', libraryName)
//					root.appendNode('description', libraryDescription)
//					root.appendNode('url', siteUrl)
//					root.children().last() + pomConfig
//
//					def dependenciesNode = root.appendNode('dependencies')
//					configurations.implementation.allDependencies.each {
//						if (it.group != null && it.name != null && it.version != null &&
//							it.name != 'unspecified' && it.version != 'unspecified') {
//							def dependencyNode = dependenciesNode.appendNode('dependency')
//							dependencyNode.appendNode('groupId', it.group)
//							dependencyNode.appendNode('artifactId', it.name)
//							dependencyNode.appendNode('version', it.version)
//						}
//					}
//				}
//			}
//
//			ProtonSDKRelease(MavenPublication) {
//				groupId = 'com.metallicus'
//				artifactId project.getName()
//				version = android.defaultConfig.versionName
//
//				artifact bundleReleaseAar
//				artifact sourcesJar
//
//				pom.withXml {
//					def root = asNode()
//					root.appendNode('name', libraryName)
//					root.appendNode('description', libraryDescription)
//					root.appendNode('url', siteUrl)
//					root.children().last() + pomConfig
//
//					def dependenciesNode = root.appendNode('dependencies')
//					configurations.implementation.allDependencies.each {
//						if (it.group != null && it.name != null && it.version != null &&
//							it.name != 'unspecified' && it.version != 'unspecified') {
//							def dependencyNode = dependenciesNode.appendNode('dependency')
//							dependencyNode.appendNode('groupId', it.group)
//							dependencyNode.appendNode('artifactId', it.name)
//							dependencyNode.appendNode('version', it.version)
//						}
//					}
//				}
//			}
//		}
//	}
//}
//
//project.afterEvaluate {
//	bintray {
//		if (project.rootProject.file('local.properties').exists()) {
//			Properties properties = new Properties()
//			properties.load(project.rootProject.file('local.properties').newDataInputStream())
//
//			user = properties.getProperty("bintray.user")
//			key = properties.getProperty("bintray.apikey")
//
//			publications = ['ProtonSDKRelease']
//
//			pkg {
//				repo = bintrayRepo
//				name = bintrayName
//				userOrg = userOrganization
//				desc = libraryDescription
//				websiteUrl = siteUrl
//				vcsUrl = gitUrl
//				licenses = allLicenses
//				publish = true
//				publicDownloadNumbers = true
//				version {
//					name = libraryVersion
//					desc = libraryDescription
//					released = new Date()
//
////					gpg {
////						sign = true //Determines whether to GPG sign the files. The default is false
////						passphrase = properties.getProperty("bintray.gpg.password")
////						//Optional. The passphrase for GPG signing'
////					}
//				}
//			}
//		}
//	}
//}
