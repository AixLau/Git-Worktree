package com.github.gitworktree.actions

import com.github.gitworktree.dialog.AddWorktreeDialog
import com.github.gitworktree.dialog.WorktreeSourceType
import com.github.gitworktree.git.GitWorktreeManager
import com.github.gitworktree.GitWorktreeBundle
import com.github.gitworktree.postcreate.PostCreateHandler
import com.github.gitworktree.toolwindow.WorktreePanel
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.vcs.log.CommitId
import com.intellij.vcs.log.VcsLogDataKeys
import git4idea.repo.GitRepositoryManager

class AddWorktreeFromLogAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val repos = GitRepositoryManager.getInstance(project).repositories
        val selectedCommit = getSelectedCommit(e) ?: return
        val repo = repos.firstOrNull { it.root == selectedCommit.root } ?: return
        val commitHash = selectedCommit.hash.asString()

        val dialog = AddWorktreeDialog(
            project = project,
            repository = repo,
            presetCommit = commitHash,
            presetSource = WorktreeSourceType.COMMIT.value,
            presetBranchName = null,
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

    override fun update(e: AnActionEvent) {
        val hash = getSelectedCommitHash(e)
        e.presentation.isEnabledAndVisible = e.project != null && hash != null
    }

    private fun getSelectedCommit(e: AnActionEvent): CommitId? = e.getData(VcsLogDataKeys.VCS_LOG_COMMIT_SELECTION)
        ?.commits
        ?.firstOrNull()

    private fun getSelectedCommitHash(e: AnActionEvent): String? {
        return getSelectedCommit(e)?.hash?.asString()
    }

    private fun notify(project: Project, content: String, type: NotificationType = NotificationType.INFORMATION) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("GitWorktree")
            .createNotification(content, type)
            .notify(project)
    }
}
