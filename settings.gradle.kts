pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.minecraftforge.net/") { name = "Forge" }
        maven("https://repo.essential.gg/repository/maven-public") { name = "Essential" }
        maven("https://maven.architectury.dev/") { name = "Architectury" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.7"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        // Root branch == the Minecraft-free `common` project, built once per version.
        versions("1.8.9", "1.21.11", "26.1.2", "26.2")

        branch("fabric") { versions("1.21.11", "26.1.2", "26.2") }
        branch("forge") { versions("1.8.9") }

        vcsVersion = "26.2"
    }
}

rootProject.name = "Neighborhood"

gradle.beforeProject {
    buildscript.repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://repo.essential.gg/repository/maven-public") { name = "Essential" }
        maven("https://maven.architectury.dev/") { name = "Architectury" }
    }
}
