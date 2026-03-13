package com.github.gitworktree.git

internal data class RemoteBranchRef(
    val remoteName: String,
    val branchName: String,
) {
    val fullName: String get() = "$remoteName/$branchName"
}

internal fun parseRemoteBranch(sourceBranch: String): RemoteBranchRef? {
    val slashIndex = sourceBranch.indexOf('/')
    if (slashIndex <= 0 || slashIndex == sourceBranch.lastIndex) {
        return null
    }

    val remoteName = sourceBranch.substring(0, slashIndex)
    val branchName = sourceBranch.substring(slashIndex + 1)
    return RemoteBranchRef(remoteName, branchName)
}

internal fun suggestLocalBranchName(sourceBranch: String, existingLocalBranchNames: Set<String>): String {
    val remoteBranch = parseRemoteBranch(sourceBranch)
    val baseName = (remoteBranch?.branchName ?: sourceBranch).ifBlank { "worktree" }
    if (baseName !in existingLocalBranchNames) {
        return baseName
    }

    val suffixedBaseName = "$baseName-worktree"
    if (suffixedBaseName !in existingLocalBranchNames) {
        return suffixedBaseName
    }

    var index = 2
    while (true) {
        val candidate = "$suffixedBaseName-$index"
        if (candidate !in existingLocalBranchNames) {
            return candidate
        }
        index += 1
    }
}
