/*
 * Pure-Java domain module.
 *
 * Hard constraint: this module must remain free of Spring, JPA, Jackson,
 * and any other framework. It models the business invariants and exposes
 * value objects + aggregates consumed by the application layer.
 *
 * Only `java-library` is applied — no Spring Boot plugin — so the produced
 * jar contains nothing but POJOs and value objects.
 */
plugins {
    `java-library`
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.26.3")
}
