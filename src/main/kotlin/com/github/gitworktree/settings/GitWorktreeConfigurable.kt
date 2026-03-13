package com.github.gitworktree.settings

import com.github.gitworktree.GitWorktreeBundle
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class GitWorktreeConfigurable(private val project: Project) : BoundConfigurable(GitWorktreeBundle.message("settings.title")) {

    private val settings get() = GitWorktreeSettings.getInstance(project)

    override fun createPanel(): DialogPanel = panel {
        group(GitWorktreeBundle.message("settings.group.general")) {
            row(GitWorktreeBundle.message("settings.default.path")) {
                textField()
                    .bindText(settings.state::defaultPathTemplate)
                    .comment(GitWorktreeBundle.message("settings.default.path.tooltip"))
            }
        }

        group(GitWorktreeBundle.message("settings.group.post.create")) {
            row {
                checkBox(GitWorktreeBundle.message("settings.copy.idea"))
                    .bindSelected(settings.state::copyIdeaByDefault)
            }
            row {
                checkBox(GitWorktreeBundle.message("settings.copy.worktree.files"))
                    .bindSelected(settings.state::copyWorktreeFilesByDefault)
            }
            row {
                checkBox(GitWorktreeBundle.message("settings.open.after"))
                    .bindSelected(settings.state::openAfterCreationByDefault)
            }
            row {
                checkBox(GitWorktreeBundle.message("settings.run.external.tool"))
                    .bindSelected(settings.state::runExternalToolByDefault)
            }
            row(GitWorktreeBundle.message("settings.external.tool.command")) {
                textField()
                    .bindText(settings.state::defaultExternalTool)
            }
        }
    }
}
