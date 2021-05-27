import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

buildscript {
	repositories {
		google()
		mavenCentral()
	}

	dependencies {
		classpath(BuildPlugins.androidGradlePlugin)
	}
}

plugins {
	kotlin("jvm") version kotlinVersion
	id(BuildPlugins.dokka) version BuildPlugins.Versions.dokka
	id(BuildPlugins.orchid) version orchidVersion
	`maven-publish`
}

allprojects {
	repositories {
		mavenLocal()

		google()
		mavenCentral()

		maven {
			name = "ProtonKotlinSdk"
			url = uri("https://maven.pkg.github.com/ProtonProtocol/ProtonKotlin")
			credentials {
				username = gradleLocalProperties(rootDir).getProperty("github.username")
				password = gradleLocalProperties(rootDir).getProperty("github.token")
			}
		}
	}
}

dependencies {
	orchidRuntimeOnly(Libraries.orchidDocs)
	orchidRuntimeOnly(Libraries.orchidKotlindoc)
	orchidRuntimeOnly(Libraries.orchidPluginDocs)
	orchidRuntimeOnly(Libraries.orchidGithub)
}

orchid {
	theme = "Editorial"
	version = ProtonSdk.versionName
	srcDir  = "protonsdk/src/orchid/resources"
	destDir = "protonsdk/build/docs/orchid"
	baseUrl = "https://protonprotocol.github.io/ProtonKotlin"

	githubToken = gradleLocalProperties(rootDir).getProperty("github.token")
}

tasks.register("cleanAll").configure {
	delete("build", "buildSrc/build", "protonsdk/build")
}