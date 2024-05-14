plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.cjcrafter"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.joml:joml:1.10.5")
    implementation("de.m3y.kformat:kformat:0.11")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}