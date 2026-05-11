/*
 * Persistence adapter module.
 *
 * Maps the pure domain aggregates onto JPA entities, exposes Spring Data
 * repositories, and wires Flyway migrations. The Spring Boot starter for
 * data-jpa is brought in as `implementation` so its types stay invisible
 * to the application layer.
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

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")
    testImplementation("org.assertj:assertj-core")
}
