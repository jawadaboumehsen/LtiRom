package org.lti.rom

/**
 * Represents a file or directory within the WSL file system.
 *
 * @property name The name of the file or directory.
 * @property path The full path to the file or directory.
 * @property isDirectory True if this is a directory, false otherwise.
 */
data class WslFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean
)
