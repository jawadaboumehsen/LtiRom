# WSL Troubleshooting Guide

## Common Issues and Solutions

### Issue: "Command failed with exit code -1"

This usually means WSL is not properly configured or accessible. Here are the solutions:

#### 1. Check WSL Installation

Open Command Prompt or PowerShell as Administrator and run:

```cmd
# Check WSL status
wsl --status

# List installed distributions
wsl --list --verbose

# If no distributions, install one
wsl --install Ubuntu
```

#### 2. Verify WSL is in PATH

The app tries these WSL commands in order:
- `wsl`
- `wsl.exe` 
- `C:\Windows\System32\wsl.exe`

To verify WSL is accessible:

```cmd
# Test from command prompt
wsl --version
wsl echo "Hello from WSL"
```

#### 3. Check WSL Distribution Status

```cmd
# List all distributions
wsl --list --all

# Start a specific distribution
wsl -d Ubuntu

# Check if distribution is running
wsl --list --running
```

#### 4. Fix Common WSL Issues

**If WSL is not installed:**
```cmd
# Enable WSL feature
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

# Restart computer, then install WSL
wsl --install
```

**If WSL is installed but not working:**
```cmd
# Update WSL
wsl --update

# Set WSL 2 as default
wsl --set-default-version 2

# Convert existing distribution to WSL 2
wsl --set-version Ubuntu 2
```

#### 5. Test WSL Manually

Try these commands in Command Prompt:

```cmd
# Basic WSL test
wsl echo "Test"

# Check WSL version
wsl --version

# List distributions
wsl --list --verbose

# Test specific command
wsl pwd
wsl ls -la
```

### Issue: "WSL Not Available"

This means the app cannot find the WSL command. Solutions:

1. **Add WSL to PATH** (if not already there):
   - Add `C:\Windows\System32` to your system PATH
   - Restart the application

2. **Use full path** in the app:
   - The app automatically tries `C:\Windows\System32\wsl.exe`

3. **Reinstall WSL**:
   ```cmd
   wsl --unregister Ubuntu
   wsl --install Ubuntu
   ```

### Issue: "Connection Test Failed"

This means WSL is found but commands are failing:

1. **Check WSL distribution**:
   ```cmd
   wsl --list --verbose
   ```

2. **Start the distribution**:
   ```cmd
   wsl -d Ubuntu
   ```

3. **Test basic commands**:
   ```cmd
   wsl echo "Hello"
   wsl pwd
   ```

### Issue: "Permission Denied" or "Access Denied"

1. **Run as Administrator**:
   - Run the LtiRom app as Administrator
   - Or run Command Prompt as Administrator and test WSL

2. **Check WSL permissions**:
   ```cmd
   wsl --status
   ```

### Debugging Steps

1. **Test in Command Prompt first**:
   ```cmd
   wsl pwd
   wsl ls -la
   wsl whoami
   ```

2. **Check WSL logs**:
   ```cmd
   wsl --status
   ```

3. **Restart WSL service**:
   ```cmd
   wsl --shutdown
   wsl --list --verbose
   ```

4. **Use the Test button** in the app to run: `echo 'Testing WSL connection...'`

### Quick Fixes

**If nothing works, try this sequence:**

1. **Shutdown WSL**:
   ```cmd
   wsl --shutdown
   ```

2. **Restart WSL**:
   ```cmd
   wsl --list --verbose
   ```

3. **Test basic command**:
   ```cmd
   wsl echo "test"
   ```

4. **If successful, try the app again**

### Alternative: Use SSH Connection

If direct WSL commands don't work, try the SSH connection method:

1. **Set up SSH in WSL**:
   ```bash
   sudo apt update
   sudo apt install openssh-server
   sudo service ssh start
   sudo passwd yourusername
   ```

2. **Connect via SSH** in the app:
   - Host: `localhost`
   - Port: `22`
   - Username: Your WSL username
   - Password: Your WSL password

### Still Having Issues?

1. **Check Windows version**: WSL requires Windows 10 version 2004 or later
2. **Check virtualization**: Ensure virtualization is enabled in BIOS
3. **Check Windows features**: Ensure "Windows Subsystem for Linux" is enabled
4. **Restart computer** after enabling WSL features
5. **Update Windows** to the latest version

### Test Commands to Try in the App

Once WSL is working, try these commands:

- `pwd` - Print working directory
- `ls -la` - List files
- `whoami` - Current user
- `uname -a` - System information
- `echo "Hello WSL"` - Simple echo test
- `date` - Current date and time
