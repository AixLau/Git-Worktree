import java.io.File

rootProject.name = "git-worktree-intellij"

val fullLineYamlModule = "intellij.fullLine.yaml.jar"
val disabledSuffix = ".disabled-by-git-worktree"
val transformsDir = File(gradle.gradleUserHomeDir, "caches")

if (transformsDir.isDirectory) {
    transformsDir.walkTopDown()
        .filter { candidate ->
            candidate.isFile &&
                candidate.name == fullLineYamlModule &&
                candidate.invariantSeparatorsPath.contains("/transforms/") &&
                candidate.invariantSeparatorsPath.contains("/plugins/fullLine/lib/modules/")
        }
        .forEach { moduleJar ->
            val disabledJar = File(moduleJar.parentFile, moduleJar.name + disabledSuffix)
            if (!disabledJar.exists()) {
                moduleJar.renameTo(disabledJar)
            }
        }
}
