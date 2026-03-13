package com.github.gitworktree.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "GitWorktreeSettings", storages = [Storage("gitWorktree.xml")])
class GitWorktreeSettings : PersistentStateComponent<GitWorktreeSettings.State> {

    data class State(
        var defaultPathTemplate: String = GitWorktreeSettings.DEFAULT_PATH_TEMPLATE,
        var copyIdeaByDefault: Boolean = false,
        var copyWorktreeFilesByDefault: Boolean = false,
        var openAfterCreationByDefault: Boolean = true,
        var runExternalToolByDefault: Boolean = false,
        var defaultExternalTool: String = ""
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        if (state.defaultPathTemplate.isBlank() || state.defaultPathTemplate == LEGACY_DEFAULT_PATH_TEMPLATE) {
            state.defaultPathTemplate = DEFAULT_PATH_TEMPLATE
        }
        myState = state
    }

    companion object {
        const val LEGACY_DEFAULT_PATH_TEMPLATE = "../{repo}-worktrees/{branch}"
        const val DEFAULT_PATH_TEMPLATE = "../{repo}-{branch}"
        fun getInstance(project: Project): GitWorktreeSettings = project.service()
    }
}
