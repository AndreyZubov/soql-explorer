/*
 * Web entry-point module.
 *
 * Owns the Spring Boot application class, REST controllers, security
 * configuration, JWT issuance, and the OpenAPI document. Pulls in every
 * infrastructure adapter so the runtime classpath is complete.
 */
plugins {
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    application
}

dependencies {
    implementation(project(":backend:application"))
    implementation(project(":backend:domain"))
    implementation(project(":backend:infrastructure-persistence"))
    implementation(project(":backend:infrastructure-salesforce"))
    implementation(project(":backend:infrastructure-cache"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")
}

application {
    mainClass.set("io.soqlexplorer.web.SoqlExplorerApplication")
}
