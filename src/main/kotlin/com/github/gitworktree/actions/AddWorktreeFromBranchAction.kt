package com.github.gitworktree.actions

import com.github.gitworktree.GitWorktreeBundle
import com.github.gitworktree.dialog.AddWorktreeDialog
import com.github.gitworktree.dialog.WorktreeSourceType
import com.github.gitworktree.git.GitWorktreeManager
import com.github.gitworktree.postcreate.PostCreateHandler
import com.github.gitworktree.toolwindow.WorktreePanel
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import git4idea.GitBranch
import git4idea.actions.branch.GitBranchActionsDataKeys
import git4idea.actions.branch.GitSingleBranchAction
import git4idea.repo.GitRepository

class AddWorktreeFromBranchAction : GitSingleBranchAction() {

    override fun actionPerformed(
        e: AnActionEvent,
        project: Project,
        repositories: List<GitRepository>,
        reference: GitBranch,
    ) {
        val repo = resolveRepository(e, repositories) ?: return
        val branchName = reference.name

        val dialog = AddWorktreeDialog(
            project = project,
            repository = repo,
            presetCommit = null,
            presetSource = WorktreeSourceType.BRANCH.value,
            presetBranchName = branchName,
        )

        if (dialog.showAndGet()) {
            val result = dialog.getResult()
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, GitWorktreeBundle.message("progress.creating")) {
                override fun run(indicator: ProgressIndicator) {
                    val manager = GitWorktreeManager.getInstance()
                    val cmdResult = manager.addWorktree(
                        project, repo, result.location, result.source, result.newBranch, result.lock
                    )
                    if (cmdResult.success()) {
                        PostCreateHandler.handle(project, repo, result)
                        WorktreePanel.refreshAll(project)
                        notify(project, GitWorktreeBundle.message("notification.add.success", result.location))
                    } else {
                        notify(
                            project,
                            GitWorktreeBundle.message("notification.add.failed", cmdResult.errorOutputAsJoinedString),
                            NotificationType.ERROR
                        )
                    }
                }
            })
        }
    }

    override fun updateIfEnabledAndVisible(
        e: AnActionEvent,
        project: Project,
        repositories: List<GitRepository>,
        reference: GitBranch,
    ) {
        e.presentation.isEnabledAndVisible = resolveRepository(e, repositories) != null && reference.name.isNotBlank()
    }

    private fun resolveRepository(e: AnActionEvent, repositories: List<GitRepository>): GitRepository? {
        return e.getData(GitBranchActionsDataKeys.SELECTED_REPOSITORY)
            ?: repositories.singleOrNull()
    }

    private fun notify(project: Project, content: String, type: NotificationType = NotificationType.INFORMATION) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("GitWorktree")
            .createNotification(content, type)
            .notify(project)
    }
}
