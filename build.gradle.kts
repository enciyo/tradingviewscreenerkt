plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
    `maven-publish`
}

group = "com.enciyo"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON Processing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.enciyo.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            artifactId = "tradingviewscrennerkt"
            groupId = "com.enciyo"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name = "tradingviewscrennerkt"
                description = """
                    A Kotlin library for fetching stock data from TradingView.
                    Provides functionalities to retrieve historical and real-time stock information.
                """.trimIndent()
                url = "https://github.com/enciyo/tradingviewscrennerkt"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "enciyo"
                        name = "Mustafa Kilic"
                        email = "enciyomk61@gmail.com"
                    }
                }

                scm {
                    url = "https://github.com/enciyo/tradingviewscrennerkt"
                    connection = "scm:git:git://github.com/enciyo/tradingviewscrennerkt"
                    developerConnection = "scm:git:ssh://github.com/enciyo/tradingviewscrennerkt"
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/enciyo/tradingviewscreenerkt")

            credentials {
                username = System.getenv("GitHubPackagesUsername") ?: project.findProperty("GitHubPackagesUsername")
                    ?.toString() ?: "enciyo"
                password = System.getenv("GitHubPackagesPassword") ?: project.findProperty("GitHubPackagesPassword")
                    ?.toString() ?: ""
            }
        }
    }
}

