import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
    id("dev.kikugie.loom-back-compat")
    id("com.google.devtools.ksp") version "2.3.4"
    `maven-publish`
}

group = "llc.redstone"
version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    stonecutter.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    stonecutter.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    stonecutter.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    stonecutter.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}


repositories {
    mavenLocal()
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    strictMaven(
        "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1",
        "DevAuth",
        "me.djtheredstoner"
    )
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.fabric_language_kotlin")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(requiredJava.majorVersion))
    }
    sourceSets {
        val test by getting {
            kotlin.srcDir("../../src/test/kotlin")
        }
    }
}

tasks {
    processResources {
        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep"),
            "fabric_loader" to project.property("deps.fabric_loader"),
            "fabric_language_kotlin" to project.property("deps.fabric_language_kotlin"),
            "fabric_api" to project.property("deps.fabric_api")
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }

    test {
        useJUnitPlatform()
    }
}

val generateItemModels by tasks.registering {
    val catalogue = rootProject.file("src/main/resources/neighborhood/textures.txt")
    val outputDir = layout.buildDirectory.dir("generated/itemModels")
    inputs.file(catalogue)
    outputs.dir(outputDir)
    val textureDir = rootProject.file("src/main/resources/assets/neighborhood/textures/item")
    inputs.dir(textureDir)
    doLast {
        val root = outputDir.get().asFile
        root.deleteRecursively()
        val namespace = "neighborhood"
        val items = root.resolve("assets/$namespace/items").apply { mkdirs() }
        val models = root.resolve("assets/$namespace/models/item").apply { mkdirs() }
        var written = 0
        var skipped = 0
        catalogue.forEachLine { line ->
            val parts = line.trim().split(':')
            if (parts.size < 2) return@forEachLine
            val slug = parts[0].trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
            // An id with no PNG is left without a model: pointing ITEM_MODEL at a model that
            // does not exist renders the missing-model cube, which is worse than the plain item.
            if (!textureDir.resolve("$slug.png").isFile) {
                skipped++
                return@forEachLine
            }
            if (slug != "inventory_fill") {
                items.resolve("$slug.json").writeText(
                    """{"model":{"type":"minecraft:model","model":"$namespace:item/$slug"}}""",
                )
                models.resolve("$slug.json").writeText(
                    """{"parent":"minecraft:item/generated","textures":{"layer0":"$namespace:item/$slug"}}""",
                )
            }
            written++
        }
        logger.lifecycle("Generated models for $written textures ($skipped without a PNG)")
    }
}

sourceSets.main { resources.srcDir(generateItemModels) }
