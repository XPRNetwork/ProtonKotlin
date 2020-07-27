import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

buildscript {
	repositories {
		google()
		jcenter()
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
	id(BuildPlugins.bintray) version BuildPlugins.Versions.bintray
}

allprojects {
	repositories {
		google()
		jcenter()
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