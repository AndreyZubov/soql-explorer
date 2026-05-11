/*
 * Cache adapter module skeleton.
 *
 * Caffeine-backed sObject/describe caches arrive in Step 2. Step 1
 * only reserves the module so subsequent Gradle wiring stays additive.
 */
plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.6"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

dependencies {
    api(project(":backend:application"))

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
