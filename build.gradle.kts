import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "id.irwan.magicfishing"
// Release builds pass -PreleaseVersion=<version> (from the git tag); local builds are dev builds.
version = (findProperty("releaseVersion") as String?) ?: "1.0.0-DEV"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.github.Slimefun:Slimefun4:RC-37") { isTransitive = false }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.jar { archiveClassifier.set("plain") }

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("MagicFishing-${project.version}.jar")
}

tasks.build { dependsOn(tasks.shadowJar) }
