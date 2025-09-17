package org.lti.rom

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WslViewModel(private val wslService: WslService) : ViewModel() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _isWslAvailable = MutableStateFlow(false)
    val isWslAvailable: StateFlow<Boolean> = _isWslAvailable.asStateFlow()
    
    private val _wslDistributions = MutableStateFlow<List<String>>(emptyList())
    val wslDistributions: StateFlow<List<String>> = _wslDistributions.asStateFlow()
    
    private val _commandOutput = MutableStateFlow("")
    val commandOutput: StateFlow<String> = _commandOutput.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    
    private val _currentDirectory = MutableStateFlow("/home/username")
    val currentDirectory: StateFlow<String> = _currentDirectory.asStateFlow()
    
    init {
        checkWslAvailability()
    }
    
    fun checkWslAvailability() {
        Napier.d("WslViewModel.checkWslAvailability() called")
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            try {
                Napier.d("Checking WSL availability...")
                val available = wslService.isWslAvailable()
                _isWslAvailable.value = available
                
                Napier.d("WSL available: $available")
                
                if (available) {
                    // Test the connection
                    Napier.d("WSL is available, testing connection...")
                    val testResult = wslService.testWslConnection()
                    
                    Napier.d("Connection test result - Success: ${testResult.success}, Error: ${testResult.error}")
                    
                    if (testResult.success) {
                        Napier.d("Connection test successful, loading distributions...")
                        loadWslDistributions()
                        updateCurrentDirectory()
                        _errorMessage.value = ""
                    } else {
                        Napier.e("Connection test failed: ${testResult.error}")
                        _errorMessage.value = "WSL is available but connection test failed: ${testResult.error}"
                    }
                } else {
                    Napier.e("WSL is not available")
                    _errorMessage.value = "WSL is not available on this system. Please install WSL or ensure it's in your PATH."
                }
            } catch (e: Exception) {
                Napier.e("Exception in checkWslAvailability", throwable = e)
                _errorMessage.value = "Error checking WSL availability: ${e.message}"
            } finally {
                _isLoading.value = false
                Napier.d("checkWslAvailability completed")
            }
        }
    }
    
    private fun loadWslDistributions() {
        viewModelScope.launch {
            try {
                val distributions = wslService.listWslDistributions()
                _wslDistributions.value = distributions
            } catch (e: Exception) {
                _errorMessage.value = "Error loading WSL distributions: ${e.message}"
            }
        }
    }
    
    private fun updateCurrentDirectory() {
        viewModelScope.launch {
            try {
                val currentDir = wslService.getCurrentDirectory()
                _currentDirectory.value = currentDir
                Napier.d("Updated current directory: '$currentDir'")
            } catch (e: Exception) {
                Napier.e("Error updating current directory", throwable = e)
            }
        }
    }
    
    fun connectToWsl(connection: WslService.WslConnection) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            try {
                val connected = wslService.connect(connection)
                _isConnected.value = connected
                
                if (!connected) {
                    _errorMessage.value = "Failed to connect to WSL"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Connection error: ${e.message}"
                _isConnected.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun executeCommand(command: String) {
        Napier.d("WslViewModel.executeCommand() called with: '$command'")
        Napier.d("Is connected: ${_isConnected.value}")
        Napier.d("WSL service isConnected: ${wslService.isConnected()}")
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            try {
                val result = if (_isConnected.value) {
                    Napier.d("Using SSH command execution")
                    wslService.executeCommand(command)
                } else {
                    Napier.d("Using direct WSL command execution")
                    wslService.executeWslCommand(command)
                }

                Napier.d("Command execution result - Success: ${result.success}, Exit Code: ${result.exitCode}")
                Napier.d("Command output: '${result.output}'")
                Napier.d("Command error: '${result.error}'")

                val outputText = if (result.success) {
                    result.output
                } else {
                    "Error: ${result.error}\nExit code: ${result.exitCode}"
                }

                Napier.d("Setting commandOutput to: '$outputText'")
                _commandOutput.value = outputText
                Napier.d("commandOutput state updated, current value: '${_commandOutput.value}'")

                if (!result.success) {
                    Napier.e("Command failed with exit code ${result.exitCode}")
                    _errorMessage.value = "Command failed with exit code ${result.exitCode}"
                } else {
                    Napier.d("Command executed successfully")
                }
            } catch (e: Exception) {
                Napier.e("Exception in executeCommand", throwable = e)
                _errorMessage.value = "Command execution error: ${e.message}"
                _commandOutput.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
                Napier.d("executeCommand completed")
            }
        }
    }
    
    fun disconnect() {
        wslService.disconnect()
        _isConnected.value = false
        _commandOutput.value = ""
        _errorMessage.value = ""
    }
    
    fun clearOutput() {
        _commandOutput.value = ""
        _errorMessage.value = ""
    }
    
    override fun onCleared() {
        super.onCleared()
        wslService.disconnect()
    }
}
