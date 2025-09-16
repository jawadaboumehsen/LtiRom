package org.lti.rom

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WslViewModel : ViewModel() {
    private val wslService = WslService()
    
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
        println("🔍 [DEBUG] WslViewModel.checkWslAvailability() called")
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            try {
                println("🔍 [DEBUG] Checking WSL availability...")
                val available = wslService.isWslAvailable()
                _isWslAvailable.value = available
                
                println("🔍 [DEBUG] WSL available: $available")
                
                if (available) {
                    // Test the connection
                    println("🔍 [DEBUG] WSL is available, testing connection...")
                    val testResult = wslService.testWslConnection()
                    
                    println("🔍 [DEBUG] Connection test result - Success: ${testResult.success}, Error: ${testResult.error}")
                    
                    if (testResult.success) {
                        println("✅ [DEBUG] Connection test successful, loading distributions...")
                        loadWslDistributions()
                        updateCurrentDirectory()
                        _errorMessage.value = ""
                    } else {
                        println("❌ [DEBUG] Connection test failed: ${testResult.error}")
                        _errorMessage.value = "WSL is available but connection test failed: ${testResult.error}"
                    }
                } else {
                    println("❌ [DEBUG] WSL is not available")
                    _errorMessage.value = "WSL is not available on this system. Please install WSL or ensure it's in your PATH."
                }
            } catch (e: Exception) {
                println("❌ [DEBUG] Exception in checkWslAvailability: ${e.message}")
                _errorMessage.value = "Error checking WSL availability: ${e.message}"
            } finally {
                _isLoading.value = false
                println("🔍 [DEBUG] checkWslAvailability completed")
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
                println("🔍 [DEBUG] Updated current directory: '$currentDir'")
            } catch (e: Exception) {
                println("❌ [DEBUG] Error updating current directory: ${e.message}")
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
        println("🔍 [DEBUG] WslViewModel.executeCommand() called with: '$command'")
        println("🔍 [DEBUG] Is connected: ${_isConnected.value}")
        println("🔍 [DEBUG] WSL service isConnected: ${wslService.isConnected()}")
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            
            try {
                val result = if (_isConnected.value) {
                    println("🔍 [DEBUG] Using SSH command execution")
                    wslService.executeCommand(command)
                } else {
                    println("🔍 [DEBUG] Using direct WSL command execution")
                    wslService.executeWslCommand(command)
                }

                println("🔍 [DEBUG] Command execution result - Success: ${result.success}, Exit Code: ${result.exitCode}")
                println("🔍 [DEBUG] Command output: '${result.output}'")
                println("🔍 [DEBUG] Command error: '${result.error}'")

                val outputText = if (result.success) {
                    result.output
                } else {
                    "Error: ${result.error}\nExit code: ${result.exitCode}"
                }

                println("🔍 [DEBUG] Setting commandOutput to: '$outputText'")
                _commandOutput.value = outputText
                println("🔍 [DEBUG] commandOutput state updated, current value: '${_commandOutput.value}'")

                if (!result.success) {
                    println("❌ [DEBUG] Command failed with exit code ${result.exitCode}")
                    _errorMessage.value = "Command failed with exit code ${result.exitCode}"
                } else {
                    println("✅ [DEBUG] Command executed successfully")
                }
            } catch (e: Exception) {
                println("❌ [DEBUG] Exception in executeCommand: ${e.message}")
                _errorMessage.value = "Command execution error: ${e.message}"
                _commandOutput.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
                println("🔍 [DEBUG] executeCommand completed")
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
