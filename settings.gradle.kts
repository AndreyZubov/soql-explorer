/*
 * Root settings for the SOQL Explorer multi-module Gradle build.
 *
 * The project is split into two top-level builds:
 *   - backend (Java 21, Spring Boot 3.3) using hexagonal architecture;
 *   - frontend (Vite + React) which is managed by npm and only referenced
 *     here for CI orchestration.
 *
 * Backend follows hexagonal layering:
 *   domain  -> application -> {infrastructure-*, web}
 * No higher layer is allowed to leak into a lower one; Gradle dependency
 * declarations are the single source of truth for that contract.
 */
rootProject.name = "soql-explorer"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

include(
    "backend:domain",
    "backend:application",
    "backend:infrastructure-persistence",
    "backend:infrastructure-salesforce",
    "backend:infrastructure-cache",
    "backend:web"
)
