import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.10"
}

group = prop("mod.group")
version = "${prop("mod.version")}+${stonecutter.current.version}"

base.archivesName = "${prop("mod.id")}-common"

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
    jvmToolchain(8)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.build {
    group = "versioned"
    description = "Must be run through 'chiseledBuild'"
}

fun prop(key: String): String = providers.gradleProperty(key).get()
