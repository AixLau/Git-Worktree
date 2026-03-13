package com.github.gitworktree.toolwindow

import com.github.gitworktree.GitWorktreeBundle
import com.github.gitworktree.GitWorktreeIcons
import com.github.gitworktree.model.WorktreeInfo
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

data class RepoNode(val repoName: String, val repoPath: String)
data class WorktreeNode(val info: WorktreeInfo, val repoPath: String)

class WorktreeTreeCellRenderer : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ) {
        val node = value as? DefaultMutableTreeNode ?: return
        when (val userObject = node.userObject) {
            is RepoNode -> renderRepoNode(userObject)
            is WorktreeNode -> renderWorktreeNode(userObject)
        }
    }

    private fun renderRepoNode(repo: RepoNode) {
        icon = GitWorktreeIcons.WorktreeMain
        append(repo.repoName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    private fun renderWorktreeNode(wt: WorktreeNode) {
        val info = wt.info

        icon = when {
            info.isLocked -> GitWorktreeIcons.WorktreeLocked
            info.isMain -> GitWorktreeIcons.WorktreeMain
            info.branch == null -> GitWorktreeIcons.WorktreeDetached
            else -> GitWorktreeIcons.WorktreeBranch
        }

        val branchLabel = info.branch ?: GitWorktreeBundle.message("panel.detached.head")
        append(branchLabel, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        append("  ${info.path}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }
}
