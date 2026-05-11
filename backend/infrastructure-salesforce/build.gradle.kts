/*
 * Salesforce adapter module skeleton.
 *
 * Step 1 leaves this module almost empty — the real WebClient, OAuth, and
 * token cipher live in Step 2. Declaring the project now keeps the
 * settings.gradle stable and lets us reserve the package layout.
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
    api(project(":backend:domain"))

    // Reserved for Step 2 — declared here so the empty module still compiles.
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
