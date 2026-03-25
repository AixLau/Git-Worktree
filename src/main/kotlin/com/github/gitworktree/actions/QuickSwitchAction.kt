package com.github.gitworktree.actions

import com.github.gitworktree.GitWorktreeBundle
import com.github.gitworktree.GitWorktreeDataKeys
import com.github.gitworktree.git.GitWorktreeManager
import com.github.gitworktree.model.WorktreeInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import git4idea.repo.GitRepositoryManager
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class QuickSwitchAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        e.getData(GitWorktreeDataKeys.SELECTED_WORKTREE)?.let { selectedWorktree ->
            openWorktreeAsProject(project, selectedWorktree)
            return
        }
        val dataContext = e.dataContext
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, GitWorktreeBundle.message("quick.switch.title"), false) {
            override fun run(indicator: ProgressIndicator) {
                val worktrees = collectWorktrees(project)
                if (worktrees.isEmpty()) {
                    return
                }

                ApplicationManager.getApplication().invokeLater {
                    if (project.isDisposed) {
                        return@invokeLater
                    }

                    val popup = JBPopupFactory.getInstance().createListPopup(
                        object : BaseListPopupStep<WorktreeInfo>(
                            GitWorktreeBundle.message("quick.switch.title"),
                            worktrees
                        ) {
                            override fun getTextFor(value: WorktreeInfo): String {
                                val branchDisplay = value.branch?.substringAfterLast("/")
                                    ?: GitWorktreeBundle.message("panel.detached.head")
                                return "$branchDisplay  (${value.path})"
                            }

                            override fun onChosen(selectedValue: WorktreeInfo, finalChoice: Boolean): PopupStep<*>? {
                                if (finalChoice) {
                                    openWorktreeAsProject(project, selectedValue)
                                }
                                return PopupStep.FINAL_CHOICE
                            }
                        }
                    )
                    popup.showInBestPositionFor(dataContext)
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    private fun collectWorktrees(project: Project): List<WorktreeInfo> {
        val manager = GitWorktreeManager.getInstance()
        val repos = GitRepositoryManager.getInstance(project).repositories
        val openProjectRoots = ProjectManager.getInstance().openProjects
            .mapNotNull { it.basePath }
            .map { File(it).absolutePath }
            .toSet()

        return repos.flatMap { repo ->
            manager.listWorktrees(project, repo)
                .filter { !it.isMain && File(it.path).absolutePath !in openProjectRoots }
        }
    }

    companion object {
        fun openWorktreeAsProject(project: Project, worktree: WorktreeInfo) {
            val path = Path.of(worktree.path)
            ProjectUtil.openOrImport(path, OpenProjectTask {
                forceOpenInNewFrame = true
                projectToClose = null
                runConfigurators = true
            })
            importMavenProjectsWhenOpened(worktree.path)
        }

        private fun importMavenProjectsWhenOpened(worktreePath: String) {
            ApplicationManager.getApplication().executeOnPooledThread {
                repeat(20) {
                    val openedProject = ProjectManager.getInstance().openProjects
                        .firstOrNull { it.basePath == worktreePath && !it.isDisposed }
                    if (openedProject != null) {
                        importMavenProjects(openedProject, worktreePath)
                        return@executeOnPooledThread
                    }
                    TimeUnit.MILLISECONDS.sleep(500)
                }
            }
        }

        private fun importMavenProjects(project: Project, worktreePath: String) {
            val baseDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(worktreePath) ?: return
            val pomFiles = baseDir.children
                ?.filter { !it.isDirectory && it.name.equals("pom.xml", ignoreCase = true) }
                .orEmpty()
            if (pomFiles.isEmpty()) {
                return
            }

            ApplicationManager.getApplication().invokeLater {
                if (project.isDisposed) {
                    return@invokeLater
                }

                runCatching {
                    val managerClass = Class.forName("org.jetbrains.idea.maven.project.MavenProjectsManager")
                    val getInstance = managerClass.getMethod("getInstance", Project::class.java)
                    val manager = getInstance.invoke(null, project)
                    val importMethod = managerClass.getMethod("addManagedFilesOrUnignore", java.util.List::class.java)
                    importMethod.invoke(manager, pomFiles)
                }
            }
        }
    }
}
