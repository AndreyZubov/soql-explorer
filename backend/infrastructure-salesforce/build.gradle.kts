/*
 * Salesforce adapter module.
 *
 * Implements the SalesforceGatewayPort, SalesforceOAuthPort, AccessTokenPort,
 * ApiUsagePort, and TokenCipherPort outbound ports. Uses Spring WebClient with
 * Resilience4j for retries + circuit breaking, AES-GCM for refresh-token
 * encryption at rest, and an in-memory Caffeine cache for access tokens.
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

val resilience4jVersion = "2.2.0"

dependencies {
    api(project(":backend:application"))
    api(project(":backend:domain"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation("io.github.resilience4j:resilience4j-spring-boot3:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-reactor:$resilience4jVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.wiremock:wiremock-standalone:3.9.1")
    testImplementation("org.assertj:assertj-core")
}
