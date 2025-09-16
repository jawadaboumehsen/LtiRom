package org.lti.rom

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class WslViewModelTest {

    private lateinit var viewModel: WslViewModel
    private lateinit var wslService: WslService

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        wslService = mockk(relaxed = true)
        viewModel = WslViewModel(wslService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `openFileBrowser should open dialog and load files`() = runTest {
        // Given
        val path = "/home/ubuntu"
        val files = listOf(WslFile("test.txt", "/home/ubuntu/test.txt", false))
        coEvery { wslService.listFiles(path) } returns files

        // When
        viewModel.openFileBrowser()

        // Then
        val state = viewModel.fileBrowserState.value
        assertTrue(state.isOpen)
        assertEquals(path, state.currentPath)
        assertEquals(files, state.files)
    }

    @Test
    fun `closeFileBrowser should close the dialog`() {
        // When
        viewModel.closeFileBrowser()

        // Then
        assertFalse(viewModel.fileBrowserState.value.isOpen)
    }

    @Test
    fun `navigateToFile should update path and files`() = runTest {
        // Given
        val newPath = "/home/ubuntu/projects"
        val newFiles = listOf(WslFile("project1", "/home/ubuntu/projects/project1", true))
        coEvery { wslService.listFiles(newPath) } returns newFiles

        // When
        viewModel.navigateToFile(newPath)

        // Then
        val state = viewModel.fileBrowserState.value
        assertEquals(newPath, state.currentPath)
        assertEquals(newFiles, state.files)
    }

    @Test
    fun `onFolderSelected should update current directory and close dialog`() {
        // Given
        val selectedPath = "/home/ubuntu/selected"

        // When
        viewModel.onFolderSelected(selectedPath)

        // Then
        assertEquals(selectedPath, viewModel.currentDirectory.value)
        assertFalse(viewModel.fileBrowserState.value.isOpen)
    }

    @Test
    fun `checkAndCloneRepository should do nothing if repo is ready`() = runTest {
        // Given
        coEvery { wslService.checkGitRepository(any()) } returns true
        coEvery { wslService.checkSubmodules(any()) } returns true

        // When
        viewModel.checkAndCloneRepository()

        // Then
        assertEquals("Repository is ready.", viewModel.repoStatus.value)
    }

    @Test
    fun `checkAndCloneRepository should init submodules if not initialized`() = runTest {
        // Given
        coEvery { wslService.checkGitRepository(any()) } returns true
        coEvery { wslService.checkSubmodules(any()) } returns false
        coEvery { wslService.initializeSubmodules(any()) } returns WslService.CommandResult("", "", 0, true)

        // When
        viewModel.checkAndCloneRepository()

        // Then
        assertEquals("Submodules initialized successfully.", viewModel.repoStatus.value)
    }

    @Test
    fun `checkAndCloneRepository should clone repo if not exists`() = runTest {
        // Given
        coEvery { wslService.checkGitRepository(any()) } returns false
        coEvery { wslService.cloneRepository(any(), any(), any()) } returns WslService.CommandResult("", "", 0, true)

        // When
        viewModel.checkAndCloneRepository()

        // Then
        assertEquals("Repository cloned successfully.", viewModel.repoStatus.value)
    }
}
