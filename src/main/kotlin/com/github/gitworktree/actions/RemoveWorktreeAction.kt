package com.github.gitworktree.actions

import com.github.gitworktree.GitWorktreeBundle
import com.github.gitworktree.GitWorktreeDataKeys
import com.github.gitworktree.dialog.RemoveWorktreeDialog
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

class RemoveWorktreeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val worktree = e.getData(GitWorktreeDataKeys.SELECTED_WORKTREE) ?: return
        val repo = e.getData(GitWorktreeDataKeys.SELECTED_REPOSITORY)
            ?: GitRepositoryManager.getInstance(project).repositories.firstOrNull()
            ?: return

        val dialog = RemoveWorktreeDialog(project, worktree)
        if (!dialog.showAndGet()) return

        val result = dialog.getResult()
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, GitWorktreeBundle.message("progress.removing")) {
            override fun run(indicator: ProgressIndicator) {
                val cmdResult = GitWorktreeManager.getInstance().removeWorktree(
                    project, repo, worktree.path, result.force
                )
                if (cmdResult.success()) {
                    WorktreePanel.refreshAll(project)
                    notify(project, GitWorktreeBundle.message("notification.remove.success"))
                } else {
                    notify(
                        project,
                        GitWorktreeBundle.message("notification.remove.failed", cmdResult.errorOutputAsJoinedString),
                        NotificationType.ERROR
                    )
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val worktree = e.getData(GitWorktreeDataKeys.SELECTED_WORKTREE)
        e.presentation.isEnabledAndVisible = worktree != null && !worktree.isMain
    }

    private fun notify(project: Project, content: String, type: NotificationType = NotificationType.INFORMATION) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("GitWorktree")
            .createNotification(content, type)
            .notify(project)
    }
}
