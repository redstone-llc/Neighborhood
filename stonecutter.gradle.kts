plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.8.9" /* [SC] DO NOT EDIT */

/**
 * Aggregate builds. `stonecutter.tasks.named` collects the same task across every node that
 * matches the filter; the root ("common") branch is excluded because it has no jar of its
 * own to collect - it is merged into each loader jar instead.
 */
fun collectorsFor(branchId: String?) =
    stonecutter.tasks.named("buildAndCollect") {
        branch.id.isNotEmpty() && (branchId == null || branch.id == branchId)
    }

tasks.register("chiseledBuild") {
    group = "project"
    description = "Builds every loader/version pair into build/libs/<mod.version>/<loader>"
    dependsOn(collectorsFor(null).map { it.values })
}

// One aggregate per loader, for when only one side needs rebuilding.
for (branch in stonecutter.tree.branches) {
    if (branch.id.isEmpty()) continue
    val name = branch.id.replaceFirstChar(Char::uppercase)
    tasks.register("chiseledBuild$name") {
        group = "project"
        description = "Builds every $name version"
        dependsOn(collectorsFor(branch.id).map { it.values })
    }
}

// runActiveClientFabric / runActiveClientForge, for whichever version is currently active.
for (node in stonecutter.tree.nodes) {
    if (node.metadata != stonecutter.current || node.branch.id.isEmpty()) continue
    val name = node.branch.id.replaceFirstChar(Char::uppercase)
    tasks.register("runActiveClient$name") {
        group = "project"
        description = "Runs the ${node.metadata.version} $name client"
        dependsOn("${node.branch.id}:${node.metadata.project}:runClient")
    }
}
