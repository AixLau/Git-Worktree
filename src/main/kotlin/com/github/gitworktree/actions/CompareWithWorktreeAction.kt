package com.github.gitworktree.actions

import com.github.gitworktree.GitWorktreeBundle
import com.github.gitworktree.git.GitWorktreeManager
import com.github.gitworktree.model.WorktreeInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager

class CompareWithWorktreeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (currentFile.isDirectory) return
        val repo = findRepository(project, currentFile) ?: return
        val dataContext = e.dataContext

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, GitWorktreeBundle.message("compare.select.worktree"), false) {
            override fun run(indicator: ProgressIndicator) {
                val worktrees = collectWorktrees(project, repo)
                if (worktrees.isEmpty()) {
                    return
                }

                ApplicationManager.getApplication().invokeLater {
                    if (project.isDisposed) {
                        return@invokeLater
                    }

                    val popup = JBPopupFactory.getInstance().createListPopup(
                        object : BaseListPopupStep<WorktreeInfo>(
                            GitWorktreeBundle.message("compare.select.worktree"),
                            worktrees
                        ) {
                            override fun getTextFor(value: WorktreeInfo): String {
                                val branchDisplay = value.branch?.substringAfterLast("/")
                                    ?: GitWorktreeBundle.message("panel.detached.head")
                                return "$branchDisplay  (${value.path})"
                            }

                            override fun onChosen(selectedValue: WorktreeInfo, finalChoice: Boolean): PopupStep<*>? {
                                if (finalChoice) {
                                    compareFileWithWorktree(project, currentFile, selectedValue)
                                }
                                return PopupStep.FINAL_CHOICE
                            }
                        }
                    )
                    popup.showInBestPositionFor(dataContext)
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = project != null && file != null && !file.isDirectory
    }

    private fun collectWorktrees(project: Project, repository: GitRepository): List<WorktreeInfo> {
        val manager = GitWorktreeManager.getInstance()
        return manager.listWorktrees(project, repository).filter { !it.isMain }
    }

    private fun compareFileWithWorktree(project: Project, currentFile: VirtualFile, worktree: WorktreeInfo) {
        val repo = findRepository(project, currentFile) ?: return
        val relativePath = currentFile.path.removePrefix(repo.root.path)
        val targetPath = worktree.path + relativePath
        val targetFile = LocalFileSystem.getInstance().findFileByPath(targetPath)

        if (targetFile == null) {
            com.intellij.openapi.ui.Messages.showWarningDialog(
                project,
                GitWorktreeBundle.message("compare.file.not.found", targetPath),
                GitWorktreeBundle.message("compare.dialog.title")
            )
            return
        }

        val contentFactory = DiffContentFactory.getInstance()
        val sourceContent = contentFactory.create(project, currentFile)
        val targetContent = contentFactory.create(project, targetFile)

        val branchDisplay = worktree.branch?.substringAfterLast("/") ?: GitWorktreeBundle.message("panel.detached")
        val request = SimpleDiffRequest(
            GitWorktreeBundle.message("compare.request.title", currentFile.name),
            sourceContent,
            targetContent,
            GitWorktreeBundle.message("compare.current.file", currentFile.path),
            GitWorktreeBundle.message("compare.target.file", branchDisplay, targetFile.path)
        )

        DiffManager.getInstance().showDiff(project, request)
    }

    private fun findRepository(project: Project, currentFile: VirtualFile): GitRepository? {
        return GitRepositoryManager.getInstance(project).repositories
            .firstOrNull { currentFile.path.startsWith(it.root.path) }
    }
}
