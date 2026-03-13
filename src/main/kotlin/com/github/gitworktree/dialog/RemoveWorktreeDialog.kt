package com.github.gitworktree.dialog

import com.github.gitworktree.GitWorktreeBundle
import com.github.gitworktree.model.WorktreeInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import javax.swing.JComponent

data class RemoveWorktreeResult(val force: Boolean)

class RemoveWorktreeDialog(
    project: Project,
    private val worktreeInfo: WorktreeInfo,
) : DialogWrapper(project) {

    private val forceUncleanCheckBox = JBCheckBox(
        GitWorktreeBundle.message("dialog.remove.force.unclean")
    )

    private val forceLockedCheckBox = JBCheckBox(
        GitWorktreeBundle.message("dialog.remove.force.locked")
    ).apply {
        isEnabled = worktreeInfo.isLocked
    }

    init {
        title = GitWorktreeBundle.message("dialog.remove.title")
        setOKButtonText(GitWorktreeBundle.message("action.remove.confirm"))
        init()
    }

    override fun createCenterPanel(): JComponent {
        val branchDisplay = worktreeInfo.branch ?: GitWorktreeBundle.message("dialog.remove.detached.head")
        return FormBuilder.createFormBuilder()
            .addComponent(JBLabel(GitWorktreeBundle.message("dialog.remove.confirm")))
            .addVerticalGap(8)
            .addComponent(JBLabel(GitWorktreeBundle.message("dialog.remove.path", worktreeInfo.path)))
            .addComponent(JBLabel(GitWorktreeBundle.message("dialog.remove.branch", branchDisplay)))
            .addVerticalGap(8)
            .addComponent(forceUncleanCheckBox)
            .addComponent(forceLockedCheckBox)
            .panel.apply {
                border = JBUI.Borders.empty(8)
            }
    }

    fun getResult(): RemoveWorktreeResult {
        return RemoveWorktreeResult(
            force = forceUncleanCheckBox.isSelected || forceLockedCheckBox.isSelected
        )
    }
}
