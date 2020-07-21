rootProject.name = "Proton Kotlin SDK"

include(":protonsdk")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}