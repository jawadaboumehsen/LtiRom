package org.lti.rom

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsService {

    private val settings: Settings = Settings()
    private val xorKey = "lti-rom"

    private fun xorEncryptDecrypt(input: String): String {
        val output = StringBuilder()
        for (i in input.indices) {
            output.append((input[i].code xor xorKey[i % xorKey.length].code).toChar())
        }
        return output.toString()
    }

    fun saveSettings(
        host: String,
        port: Int,
        username: String,
        password: String,
        workDir: String,
        repoUrl: String,
        branch: String,
        selectedTargetDevice: String
    ) {
        settings[HOST_KEY] = xorEncryptDecrypt(host)
        settings[PORT_KEY] = port
        settings[USERNAME_KEY] = xorEncryptDecrypt(username)
        settings[PASSWORD_KEY] = xorEncryptDecrypt(password)
        settings[WORK_DIR_KEY] = xorEncryptDecrypt(workDir)
        settings[REPO_URL_KEY] = xorEncryptDecrypt(repoUrl)
        settings[BRANCH_KEY] = xorEncryptDecrypt(branch)
        settings[SELECTED_TARGET_DEVICE_KEY] = xorEncryptDecrypt(selectedTargetDevice)
    }

    fun getSettings(): Map<String, Any?> {
        val host = settings.getStringOrNull(HOST_KEY)?.let { xorEncryptDecrypt(it) }
        val port = settings.getIntOrNull(PORT_KEY)
        val username = settings.getStringOrNull(USERNAME_KEY)?.let { xorEncryptDecrypt(it) }
        val password = settings.getStringOrNull(PASSWORD_KEY)?.let { xorEncryptDecrypt(it) }
        val workDir = settings.getStringOrNull(WORK_DIR_KEY)?.let { xorEncryptDecrypt(it) }
        val repoUrl = settings.getStringOrNull(REPO_URL_KEY)?.let { xorEncryptDecrypt(it) }
        val branch = settings.getStringOrNull(BRANCH_KEY)?.let { xorEncryptDecrypt(it) }
        val selectedTargetDevice = settings.getStringOrNull(SELECTED_TARGET_DEVICE_KEY)?.let { xorEncryptDecrypt(it) }

        return mapOf(
            HOST_KEY to host,
            PORT_KEY to port,
            USERNAME_KEY to username,
            PASSWORD_KEY to password,
            WORK_DIR_KEY to workDir,
            REPO_URL_KEY to repoUrl,
            BRANCH_KEY to branch,
            SELECTED_TARGET_DEVICE_KEY to selectedTargetDevice
        )
    }

    fun settingsExist(): Boolean {
        return settings.hasKey(HOST_KEY)
    }

    companion object {
        const val HOST_KEY = "host"
        const val PORT_KEY = "port"
        const val USERNAME_KEY = "username"
        const val PASSWORD_KEY = "password"
        const val WORK_DIR_KEY = "work_dir"
        const val REPO_URL_KEY = "repo_url"
        const val BRANCH_KEY = "branch"
        const val SELECTED_TARGET_DEVICE_KEY = "selected_target_device"
    }
}
