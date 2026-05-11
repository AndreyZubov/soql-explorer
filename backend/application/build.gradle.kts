/*
 * Application module.
 *
 * Holds inbound use-case interfaces and outbound port interfaces. The only
 * compile dependency is the domain module — application code must never
 * import Spring, JPA, Jackson, or any infrastructure type.
 */
plugins {
    `java-library`
}

dependencies {
    api(project(":backend:domain"))

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}
