# Git Worktree

`Git Worktree` is an IntelliJ IDEA plugin for managing Git worktrees without leaving the IDE.

## Features

- Create a worktree from the current repository
- Create a worktree from the Git branches popup
- Create a worktree from a selected VCS Log commit
- View all worktrees in a dedicated tool window
- Remove, lock, and unlock worktrees
- Quickly open another worktree as a project
- Compare the current file with the same file in another worktree
- Configure default path templates and post-create actions
- Optionally copy `.idea` and files listed in `.worktree-copy`
- Optionally run an external command after worktree creation

## Compatibility

- IntelliJ IDEA `2025.3`
- Build range `253.*`
- Java `21`

## Install

1. Build or download the plugin ZIP.
2. In IntelliJ IDEA, open `Settings` > `Plugins`.
3. Click the gear icon, then `Install Plugin from Disk...`.
4. Select the built ZIP package.
5. Restart IntelliJ IDEA.

Current local build artifact:

- `build/distributions/git-worktree-intellij-1.0.0.zip`

## Main Entry Points

- `Git` menu > `Worktree`
- Git branches popup > `Create Worktree...`
- VCS Log context menu > `Create Worktree...`
- Tool window: `Git Worktree`
- Editor / Project View context menu > `Compare With Worktree...`

## Usage

### Create a worktree

1. Open a Git repository in IntelliJ IDEA.
2. Trigger `Create Worktree...` from one of the plugin entry points.
3. Choose the source:
   - `HEAD`
   - local branch
   - remote branch
   - commit
   - tag
4. Confirm the target path.
5. Optionally enable:
   - create a new branch
   - lock after creation
   - copy `.idea`
   - copy `.worktree-copy` files
   - run an external tool
   - open the worktree as a project

### Worktree tool window

The tool window shows repositories and their worktrees. From there you can:

- refresh
- remove a worktree
- lock or unlock a worktree
- double-click to open a worktree project

### Compare files across worktrees

Open a file, then use `Compare With Worktree...` to diff it with the matching file in another worktree from the same repository.

## Default Path Behavior

The default target path is a sibling directory of the current repository:

- pattern: `../{repo}-{branch}`
- example: `/path/to/agent-flow` + branch `main` -> `/path/to/agent-flow-main`

Branch names used in paths are normalized for a single directory name:

- `feature/demo` becomes `feature-demo`

You can override the template in:

- `Settings` > `Tools` > `Git Worktree`

## Remote Branch Behavior

When creating a worktree from a remote branch such as `origin/main`, the plugin avoids detached HEAD by preparing a local branch first.

Behavior:

1. Fetch the remote to refresh remote tracking refs.
2. If a same-name local branch does not exist, create it from the remote branch and create the worktree.
3. If a same-name local branch exists and is not checked out by another worktree, fast-forward it to the remote branch and use it for the new worktree.
4. If that local branch is already occupied by another worktree, or cannot be safely fast-forwarded, automatically create a derived local branch such as `main-worktree` and create the worktree from the remote branch.

This avoids the common `detached HEAD` outcome of running `git worktree add <path> origin/main` directly.

## Settings

Project-level settings include:

- default path template
- copy `.idea` by default
- copy `.worktree-copy` files by default
- open after creation by default
- run external tool by default
- default external tool command

## Development

Build:

```bash
./gradlew buildPlugin
```

Test:

```bash
./gradlew test
```
