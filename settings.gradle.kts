include ("proto", "server", "numberms")

pluginManagement {
    val kotlinVersion: String by settings
    val protobufPluginVersion: String by settings
    val springBootVersion: String by settings
    repositories {
        gradlePluginPortal()
        google()
    }
    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version "1.0.11.RELEASE"
        id("com.google.protobuf") version protobufPluginVersion apply false
        kotlin("jvm") version kotlinVersion apply false
        id("idea")
    }

}
