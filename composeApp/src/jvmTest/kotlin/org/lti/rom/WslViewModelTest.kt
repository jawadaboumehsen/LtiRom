package org.lti.rom

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class WslViewModelTest {

    private lateinit var wslService: WslService
    private lateinit var settingsService: SettingsService
    private lateinit var viewModel: WslViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        wslService = mockk()
        settingsService = mockk()
        coEvery { settingsService.settingsExist() } returns false
        coEvery { wslService.isWslAvailable() } returns true
        coEvery { wslService.testWslConnection() } returns WslService.CommandResult("", "", 0, true)
        coEvery { wslService.listWslDistributions() } returns listOf("Ubuntu")
        coEvery { wslService.getCurrentDirectory() } returns "/home/user"
        viewModel = WslViewModel(wslService, settingsService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct when no settings exist`() {
        assertEquals(Screen.SETUP, viewModel.currentScreen.value)
    }

    @Test
    fun `initial state is correct when settings exist`() {
        coEvery { settingsService.settingsExist() } returns true
        coEvery { settingsService.getSettings() } returns mapOf(
            SettingsService.HOST_KEY to "localhost",
            SettingsService.PORT_KEY to 22,
            SettingsService.USERNAME_KEY to "user",
            SettingsService.PASSWORD_KEY to "password",
            SettingsService.WORK_DIR_KEY to "/home/user",
            SettingsService.REPO_URL_KEY to "git@github.com:user/repo.git",
            SettingsService.BRANCH_KEY to "main",
            SettingsService.SELECTED_TARGET_DEVICE_KEY to "device1"
        )
        coEvery { wslService.connect(any()) } returns true

        viewModel = WslViewModel(wslService, settingsService)

        assertEquals(Screen.MAIN, viewModel.currentScreen.value)
        assertEquals("localhost", viewModel.host.value)
        assertEquals("22", viewModel.port.value)
        assertEquals("user", viewModel.username.value)
        assertEquals("password", viewModel.password.value)
        assertEquals("/home/user", viewModel.currentDirectory.value)
        assertEquals("git@github.com:user/repo.git", viewModel.gitRepoUrl.value)
        assertEquals("main", viewModel.gitBranch.value)
        assertEquals("device1", viewModel.selectedTargetDevice.value)
    }

    @Test
    fun `saveSettings should call settingsService with correct values`() {
        coEvery { settingsService.saveSettings(any(), any(), any(), any(), any(), any(), any(), any()) } returns Unit

        viewModel.saveSettings(
            host = "new-host",
            port = "22",
            username = "wsl",
            password = "",
            workDir = "/home/user",
            repoUrl = "",
            branch = "main",
            selectedTargetDevice = ""
        )

        coVerify {
            settingsService.saveSettings(
                host = "new-host",
                port = 22,
                username = "wsl",
                password = "",
                workDir = "/home/user",
                repoUrl = "",
                branch = "main",
                selectedTargetDevice = ""
            )
        }
    }

    @Test
    fun `openFileBrowser should update fileBrowserState`() {
        coEvery { wslService.listFiles(any()) } returns emptyList()
        viewModel.openFileBrowser()
        assertEquals(true, viewModel.fileBrowserState.value.isOpen)
    }

    @Test
    fun `onFolderSelected should update currentDirectory and close file browser`() {
        viewModel.onFolderSelected("/new/path")
        assertEquals("/new/path", viewModel.currentDirectory.value)
        assertEquals(false, viewModel.fileBrowserState.value.isOpen)
    }
}
