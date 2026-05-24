plugins {
    alias(libs.plugins.agp.lib)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    `maven-publish`
    signing
}

android {
    namespace = "io.github.libxposed.service.interfaces"
    compileSdk = 37
    buildToolsVersion = "37.0.0"
    androidResources.enable = false
    enableKotlin = false

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        buildConfig = false
        aidl = true
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

val libVersion = "102.0.0"
val publishSnapshot = providers.gradleProperty("publishSnapshot").orNull == "true"
fun String.real(snapshot: Boolean) = if (snapshot) "$this-SNAPSHOT" else this

val dokkaJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    dependsOn("dokkaGeneratePublicationJavadoc")
    from(layout.buildDirectory.dir("dokka/javadoc"))
}

publishing {
    publications {
        register<MavenPublication>("interface") {
            artifactId = "interface"
            group = "io.github.libxposed"
            version = libVersion.real(publishSnapshot)
            artifact(dokkaJavadocJar)
            pom {
                name.set("interface")
                description.set("Modern Xposed Service Interface")
                url.set("https://github.com/libxposed/service")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://github.com/libxposed/service/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        name.set("libxposed")
                        url.set("https://libxposed.github.io")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/libxposed/service.git")
                    url.set("https://github.com/libxposed/service")
                }
            }
            afterEvaluate {
                from(components.getByName("release"))
            }
        }
    }
    repositories {
        maven {
            name = "ossrh"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials(PasswordCredentials::class)
        }
        maven {
            name = "snapshots"
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
            credentials(PasswordCredentials::class)
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/libxposed/service")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    val signingKey = findProperty("signingKey") as String?
    val signingPassword = findProperty("signingPassword") as String?
    if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
