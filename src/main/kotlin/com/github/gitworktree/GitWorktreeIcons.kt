package com.github.gitworktree

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object GitWorktreeIcons {
    @JvmField val WorktreeBranch: Icon = IconLoader.getIcon("/icons/worktreeBranch.svg", javaClass)
    @JvmField val WorktreeDetached: Icon = IconLoader.getIcon("/icons/worktreeDetached.svg", javaClass)
    @JvmField val WorktreeLocked: Icon = IconLoader.getIcon("/icons/worktreeLocked.svg", javaClass)
    @JvmField val WorktreeMain: Icon = IconLoader.getIcon("/icons/worktreeMain.svg", javaClass)
}
