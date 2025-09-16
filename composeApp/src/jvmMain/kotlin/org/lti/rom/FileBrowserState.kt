package org.lti.rom

data class FileBrowserState(
    val isOpen: Boolean = false,
    val currentPath: String = "/home/ubuntu",
    val files: List<WslFile> = emptyList()
)
