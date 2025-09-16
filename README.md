# LtiRom - WSL Desktop Client

A Kotlin Multiplatform desktop application for connecting to and managing WSL (Windows Subsystem for Linux).

## Features

- Connect to WSL via SSH
- Execute commands directly in WSL
- View command output in real-time
- Quick access to common commands
- List available WSL distributions
- Modern Material 3 UI

## Prerequisites

- Windows 10/11 with WSL installed
- Java 11 or higher
- WSL distribution (Ubuntu, Debian, etc.)

## Building and Running

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

## Usage

1. Launch the application
2. Click "Connect" to establish a connection to WSL
3. Use the command input to execute Linux commands
4. View results in the output panel

## WSL Setup

To use SSH connection to WSL:

1. Install OpenSSH server in your WSL distribution:
   ```bash
   sudo apt update
   sudo apt install openssh-server
   ```

2. Configure SSH:
   ```bash
   sudo nano /etc/ssh/sshd_config
   # Set PasswordAuthentication yes
   # Set Port 22
   ```

3. Start SSH service:
   ```bash
   sudo service ssh start
   ```

4. Set a password for your WSL user:
   ```bash
   sudo passwd yourusername
   ```

## Architecture

- **WslService**: Handles WSL connections and command execution
- **WslViewModel**: Manages application state and business logic
- **WslInterface**: Main UI for command execution and output
- **WslConnectionDialog**: Connection configuration dialog

## Dependencies

- Compose Multiplatform for UI
- JSch for SSH connections
- Apache Commons Exec for process execution
- Kotlin Coroutines for async operations

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…