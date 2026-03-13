package com.github.gitworktree.model

data class WorktreeInfo(
    val path: String,
    val head: String,
    val branch: String?,
    val isLocked: Boolean,
    val lockReason: String?,
    val isPrunable: Boolean,
    val isBare: Boolean,
    val isMain: Boolean,
)
