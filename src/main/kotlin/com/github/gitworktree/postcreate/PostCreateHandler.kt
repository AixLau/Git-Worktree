package com.github.gitworktree.postcreate

import com.github.gitworktree.actions.QuickSwitchAction
import com.github.gitworktree.dialog.AddWorktreeResult
import com.github.gitworktree.model.WorktreeInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object PostCreateHandler {

    private val LOG = logger<PostCreateHandler>()

    fun handle(project: Project, repo: GitRepository, result: AddWorktreeResult) {
        val targetDir = File(result.location)

        if (result.copyIdea) {
            copyIdeaDirectory(repo, targetDir)
        }

        if (result.copyWorktreeFiles) {
            copyWorktreeFiles(repo, targetDir)
        }

        if (result.runExternalTool && !result.externalToolName.isNullOrBlank()) {
            runExternalTool(project, targetDir, result.externalToolName)
        }

        if (result.openAfterCreation) {
            val worktreeInfo = WorktreeInfo(
                path = result.location,
                head = "",
                branch = result.newBranch,
                isLocked = result.lock,
                lockReason = null,
                isPrunable = false,
                isBare = false,
                isMain = false
            )
            ApplicationManager.getApplication().invokeLater {
                if (!project.isDisposed) {
                    QuickSwitchAction.openWorktreeAsProject(project, worktreeInfo)
                }
            }
        }
    }

    private fun copyIdeaDirectory(repo: GitRepository, targetDir: File) {
        val sourceIdea = File(repo.root.path, ".idea")
        val targetIdea = File(targetDir, ".idea")
        if (sourceIdea.exists() && sourceIdea.isDirectory) {
            copyDirectory(sourceIdea.toPath(), targetIdea.toPath())
            LOG.info("Copied .idea directory to ${targetIdea.absolutePath}")
        }
    }

    private fun copyWorktreeFiles(repo: GitRepository, targetDir: File) {
        val copyFile = File(repo.root.path, ".worktree-copy")
        if (!copyFile.exists()) return

        val sourceRoot = copyFile.parentFile.toPath()
        copyFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .forEach { relativePath ->
                val source = sourceRoot.resolve(relativePath)
                val target = targetDir.toPath().resolve(relativePath)
                if (Files.exists(source)) {
                    if (Files.isDirectory(source)) {
                        copyDirectory(source, target)
                    } else {
                        Files.createDirectories(target.parent)
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
                    }
                    LOG.info("Copied $relativePath to worktree")
                }
            }
    }

    private fun runExternalTool(project: Project, targetDir: File, toolName: String) {
        try {
            val processBuilder = ProcessBuilder()
                .directory(targetDir)
                .command(if (System.getProperty("os.name").lowercase().contains("win")) {
                    listOf("cmd", "/c", toolName)
                } else {
                    listOf("sh", "-c", toolName)
                })
                .redirectErrorStream(true)

            val env = processBuilder.environment()
            env["NewGitWorktreeDir"] = targetDir.absolutePath

            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                LOG.warn("External tool '$toolName' exited with code $exitCode: $output")
            } else {
                LOG.info("External tool '$toolName' completed successfully")
            }
        } catch (e: Exception) {
            LOG.error("Failed to run external tool '$toolName'", e)
        }
    }

    private fun copyDirectory(source: Path, target: Path) {
        Files.walk(source).forEach { sourcePath ->
            val targetPath = target.resolve(source.relativize(sourcePath))
            if (Files.isDirectory(sourcePath)) {
                Files.createDirectories(targetPath)
            } else {
                Files.createDirectories(targetPath.parent)
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}
