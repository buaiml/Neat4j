plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "com.cjcrafter"
version = "0.2.5-SNAPSHOT"

val githubOwner = "buaiml"
val githubRepo = "Neat4j"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.joml:joml:1.10.5")
    implementation("de.m3y.kformat:kformat:0.11")

    // Saving neural networks and Neat state to file
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("OSSRH_USERNAME") ?: findProperty("OSSRH_USERNAME").toString())
            password.set(System.getenv("OSSRH_PASSWORD") ?: findProperty("OSSRH_PASSWORD").toString())
        }
    }
}


signing {
    isRequired = true
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID") ?: findProperty("SIGNING_KEY_ID").toString(),
        System.getenv("SIGNING_PRIVATE_KEY") ?: findProperty("SIGNING_PRIVATE_KEY").toString(),
        System.getenv("SIGNING_PASSWORD") ?: findProperty("SIGNING_PASSWORD").toString(),
    )
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set(githubRepo)
                description.set("Implementation of the NEAT algorithm in Kotlin for Java")
                url.set("https://github.com/$githubOwner/$githubRepo")

                groupId = group.toString()
                artifactId = githubRepo.lowercase()

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("CJCrafter")
                        name.set("Collin Barber")
                        email.set("collinjbarber@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/$githubOwner/$githubRepo.git")
                    developerConnection.set("scm:git:ssh://github.com/$githubOwner/$githubRepo.git")
                    url.set("https://github.com/$githubOwner/$githubRepo")
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}