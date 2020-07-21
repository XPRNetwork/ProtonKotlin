buildscript {
	repositories {
		jcenter()
		google()
		maven("https://dl.bintray.com/kotlin/kotlin-eap")
	}

	dependencies {
		classpath("com.android.tools.build:gradle:4.0.1")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
		classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.1")
		classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
		classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
	}
}

plugins {
	id("org.jetbrains.dokka") version "0.10.1"
	id("com.eden.orchidPlugin") version "0.21.0"
}

allprojects {
	repositories {
		jcenter()
		google()
		maven("https://dl.bintray.com/kotlin/kotlin-eap")
		maven("https://kotlin.bintray.com/kotlinx")
	}
}