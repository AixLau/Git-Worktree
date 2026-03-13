package com.github.gitworktree.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class GitWorktreeSettingsTest {

    @Test
    fun `loadState migrates legacy default path template to sibling-directory layout`() {
        val settings = GitWorktreeSettings()

        settings.loadState(
            GitWorktreeSettings.State(
                defaultPathTemplate = GitWorktreeSettings.LEGACY_DEFAULT_PATH_TEMPLATE,
            )
        )

        assertEquals(GitWorktreeSettings.DEFAULT_PATH_TEMPLATE, settings.state.defaultPathTemplate)
    }
}
