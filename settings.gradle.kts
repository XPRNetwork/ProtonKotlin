rootProject.name = "ProtonKotlinSDK"

include(":protonsdk")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}