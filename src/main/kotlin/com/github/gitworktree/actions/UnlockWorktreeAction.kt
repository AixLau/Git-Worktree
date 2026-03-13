package com.github.gitworktree.actions

import com.github.gitworktree.GitWorktreeBundle
import com.github.gitworktree.GitWorktreeDataKeys
import com.github.gitworktree.git.GitWorktreeManager
import com.github.gitworktree.toolwindow.WorktreePanel
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepositoryManager

class UnlockWorktreeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val worktree = e.getData(GitWorktreeDataKeys.SELECTED_WORKTREE) ?: return
        val repo = e.getData(GitWorktreeDataKeys.SELECTED_REPOSITORY)
            ?: GitRepositoryManager.getInstance(project).repositories.firstOrNull()
            ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, GitWorktreeBundle.message("action.unlock.worktree")) {
            override fun run(indicator: ProgressIndicator) {
                val cmdResult = GitWorktreeManager.getInstance().unlockWorktree(project, repo, worktree.path)
                if (cmdResult.success()) {
                    WorktreePanel.refreshAll(project)
                    notify(project, GitWorktreeBundle.message("notification.unlock.success"))
                } else {
                    notify(
                        project,
                        GitWorktreeBundle.message("notification.unlock.failed", cmdResult.errorOutputAsJoinedString),
                        NotificationType.ERROR
                    )
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val worktree = e.getData(GitWorktreeDataKeys.SELECTED_WORKTREE)
        e.presentation.isEnabledAndVisible = worktree != null && !worktree.isMain && worktree.isLocked
    }

    private fun notify(project: Project, content: String, type: NotificationType = NotificationType.INFORMATION) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("GitWorktree")
            .createNotification(content, type)
            .notify(project)
    }
}
