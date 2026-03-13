# Git Worktree 1.0.0

## Highlights

- Upgraded the plugin to IntelliJ IDEA `2025.3` / build `253.*`
- Moved the project to Java `21`
- Fixed several EDT and background-thread issues around Git operations
- Improved multi-repository behavior in Git branches, VCS Log, quick switch, and compare flows
- Added settings-driven default path handling and post-create defaults
- Updated the default worktree path to a sibling directory using `{repo}-{branch}`
- Normalized generated paths so they render as real sibling paths instead of `../...`
- Improved remote branch handling to avoid detached HEAD

## Remote Branch Worktree Behavior

When creating a worktree from a remote branch:

1. The plugin fetches the remote first.
2. If a same-name local branch does not exist, it creates one from the remote branch.
3. If a same-name local branch exists and is not occupied by another worktree, it fast-forwards that local branch and reuses it.
4. If the local branch is already occupied by another worktree, or cannot be safely fast-forwarded, the plugin creates a derived local branch such as `main-worktree`.

## Packaging

Expected plugin artifact:

- `build/distributions/git-worktree-intellij-1.0.0.zip`
