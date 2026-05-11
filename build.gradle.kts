/*
 * Root build script.
 *
 * Centralizes:
 *   - Java toolchain (21);
 *   - shared Spotless formatting and Checkstyle quality gates;
 *   - common test/jacoco wiring for every backend subproject.
 *
 * The root project itself produces no artifact; it only configures
 * children under `backend:*`.
 */
plugins {
    java
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("checkstyle")
    id("jacoco")
}

allprojects {
    group = "io.soqlexplorer"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    // Only configure Java subprojects. The frontend module is not registered
    // as a Gradle subproject, so this is currently a no-op guard for safety
    // in case more non-Java modules are added later.
    if (path.startsWith(":backend")) {
        apply(plugin = "java")
        apply(plugin = "java-library")
        apply(plugin = "checkstyle")
        apply(plugin = "jacoco")
        apply(plugin = "com.diffplug.spotless")

        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            // Keep the test JVM in UTC so date/time assertions are stable on CI.
            systemProperty("user.timezone", "UTC")
            testLogging {
                events("passed", "skipped", "failed")
                showExceptions = true
                showCauses = true
                showStackTraces = true
            }
        }

        extensions.configure<CheckstyleExtension> {
            toolVersion = "10.17.0"
            configFile = rootProject.file("config/checkstyle/checkstyle.xml")
            maxWarnings = 0
            isIgnoreFailures = false
        }

        // Spotless: Google Java Format keeps formatting trivial and
        // diff-friendly. Configured imperatively to avoid leaking Spotless
        // API types into this script via the typed DSL.
        extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            java {
                target("src/**/*.java")
                googleJavaFormat("1.22.0")
                removeUnusedImports()
                trimTrailingWhitespace()
                endWithNewline()
            }
            kotlinGradle {
                target("*.gradle.kts")
                ktlint("1.3.1")
            }
        }

        extensions.configure<JacocoPluginExtension> {
            toolVersion = "0.8.12"
        }

        tasks.named<Test>("test") {
            finalizedBy("jacocoTestReport")
        }

        tasks.named<JacocoReport>("jacocoTestReport") {
            dependsOn("test")
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }
    }
}
