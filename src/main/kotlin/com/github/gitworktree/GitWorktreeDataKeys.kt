package com.github.gitworktree

import com.intellij.openapi.actionSystem.DataKey
import com.github.gitworktree.model.WorktreeInfo
import git4idea.repo.GitRepository

object GitWorktreeDataKeys {
    val SELECTED_WORKTREE = DataKey.create<WorktreeInfo>("GitWorktree.SelectedWorktree")
    val SELECTED_REPOSITORY = DataKey.create<GitRepository>("GitWorktree.SelectedRepository")
}
