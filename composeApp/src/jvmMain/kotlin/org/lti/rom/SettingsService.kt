package org.lti.rom

import com.russhwolf.settings.Settings

class SettingsService {
    private val settings = Settings()
    private val xorKey = "ltirom"

    private fun encrypt(value: String): String {
        val result = StringBuilder()
        for (i in value.indices) {
            result.append((value[i].code xor xorKey[i % xorKey.length].code).toChar())
        }
        return result.toString()
    }

    private fun decrypt(value: String): String {
        return encrypt(value) // XOR encryption is symmetric
    }

    fun saveSettings(
        host: String,
        port: Int,
        username: String,
        password: String,
        workDir: String,
        repoUrl: String,
        branch: String
    ) {
        settings.putString(HOST_KEY, encrypt(host))
        settings.putInt(PORT_KEY, port)
        settings.putString(USERNAME_KEY, encrypt(username))
        settings.putString(PASSWORD_KEY, encrypt(password))
        settings.putString(WORK_DIR_KEY, encrypt(workDir))
        settings.putString(REPO_URL_KEY, encrypt(repoUrl))
        settings.putString(BRANCH_KEY, encrypt(branch))
    }

    fun getSettings(): Map<String, Any?> {
        return mapOf(
            HOST_KEY to settings.getStringOrNull(HOST_KEY)?.let { decrypt(it) },
            PORT_KEY to settings.getIntOrNull(PORT_KEY),
            USERNAME_KEY to settings.getStringOrNull(USERNAME_KEY)?.let { decrypt(it) },
            PASSWORD_KEY to settings.getStringOrNull(PASSWORD_KEY)?.let { decrypt(it) },
            WORK_DIR_KEY to settings.getStringOrNull(WORK_DIR_KEY)?.let { decrypt(it) },
            REPO_URL_KEY to settings.getStringOrNull(REPO_URL_KEY)?.let { decrypt(it) },
            BRANCH_KEY to settings.getStringOrNull(BRANCH_KEY)?.let { decrypt(it) }
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
    }
}
