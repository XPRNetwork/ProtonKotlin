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
	orchidRuntimeOnly(Libraries.orchid)
}

orchid {
	theme = "Editorial"
	version = ProtonSdk.versionName
	srcDir  = "protonsdk/src/orchid/resources"
	destDir = "protonsdk/build/docs/orchid"
}