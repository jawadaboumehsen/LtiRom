# IntelliJ IDEA Setup Guide for LtiRom WSL Desktop Client

This guide will help you set up and run the LtiRom WSL Desktop Client using IntelliJ IDEA.

## Prerequisites

- IntelliJ IDEA 2024.1 or later
- Java 11 or higher (OpenJDK 24 recommended)
- WSL installed on Windows
- Git (optional, for version control)

## Project Setup

### 1. Open Project in IntelliJ IDEA

1. Launch IntelliJ IDEA
2. Click "Open" or "Open or Import"
3. Navigate to the project directory: `C:\Users\Mohammad\IdeaProjects\LtiRom`
4. Select the project folder and click "OK"
5. IntelliJ will automatically detect it as a Gradle project

### 2. Configure Project SDK

1. Go to `File` → `Project Structure` (Ctrl+Alt+Shift+S)
2. Under `Project Settings` → `Project`, ensure:
   - Project SDK: OpenJDK 24 (or your preferred Java version)
   - Project language level: 24
3. Click "Apply" and "OK"

### 3. Gradle Configuration

The project is pre-configured with the following Gradle settings:
- Auto-import enabled
- Gradle JVM: OpenJDK 24
- Build and run using: Gradle
- Run tests using: Gradle

## Available Run Configurations

The project includes several pre-configured run configurations accessible from the run dropdown in the toolbar:

### 1. LtiRom Desktop
- **Purpose**: Run the main desktop application
- **Gradle Task**: `:composeApp:run`
- **Usage**: Click the green play button or select from run dropdown

### 2. LtiRom Debug
- **Purpose**: Run the application with debugging enabled
- **Gradle Task**: `:composeApp:run`
- **VM Options**: `-Xmx2g -XX:+UseG1GC`
- **Usage**: Set breakpoints and run in debug mode

### 3. LtiRom Build
- **Purpose**: Build the project without running
- **Gradle Task**: `:composeApp:build`
- **Usage**: Compile and check for errors

### 4. LtiRom Clean
- **Purpose**: Clean build artifacts
- **Gradle Task**: `clean`
- **Usage**: Remove all build outputs

### 5. LtiRom Test
- **Purpose**: Run unit tests
- **Gradle Task**: `:composeApp:test`
- **Usage**: Execute test suite

### 6. LtiRom Package
- **Purpose**: Create distribution package
- **Gradle Task**: `:composeApp:packageDistributionForCurrentOS`
- **Usage**: Generate installable package

## Running the Application

### Method 1: Using Run Configuration
1. Select "LtiRom Desktop" from the run dropdown
2. Click the green play button (▶️)
3. The application will build and launch

### Method 2: Using Gradle Tool Window
1. Open the Gradle tool window (View → Tool Windows → Gradle)
2. Navigate to `LtiRom` → `composeApp` → `Tasks` → `application`
3. Double-click `run`

### Method 3: Using Terminal
1. Open the terminal in IntelliJ (View → Tool Windows → Terminal)
2. Run: `./gradlew :composeApp:run` (Windows) or `./gradlew :composeApp:run` (macOS/Linux)

## Debugging

### Setting Breakpoints
1. Click in the left margin next to line numbers to set breakpoints
2. Run using "LtiRom Debug" configuration
3. The debugger will pause at breakpoints

### Debug Console
- Use the Debug Console to evaluate expressions
- Inspect variables in the Variables panel
- Step through code using debug controls

## Building and Packaging

### Build Project
- Use "LtiRom Build" configuration
- Or run: `./gradlew :composeApp:build`

### Create Distribution Package
- Use "LtiRom Package" configuration
- Or run: `./gradlew :composeApp:packageDistributionForCurrentOS`
- Output will be in `composeApp/build/compose/binaries/main/`

## Troubleshooting

### Common Issues

1. **Java Version Mismatch**
   - Ensure JAVA_HOME is set correctly
   - Check Project Structure → Project SDK

2. **Gradle Sync Issues**
   - File → Sync Project with Gradle Files
   - Invalidate Caches: File → Invalidate Caches and Restart

3. **Build Failures**
   - Clean project: Use "LtiRom Clean" configuration
   - Rebuild: Use "LtiRom Build" configuration

4. **WSL Connection Issues**
   - Ensure WSL is installed and running
   - Check WSL distribution is available
   - Verify SSH server is running (for SSH connections)

### Performance Optimization

1. **Increase Memory**
   - File → Settings → Build, Execution, Deployment → Compiler
   - Increase "Build process heap size" to 2048 MB

2. **Enable Parallel Compilation**
   - Already configured in `.idea/compiler.xml`

3. **Gradle Daemon**
   - Ensure Gradle daemon is enabled (default)
   - Check in Gradle settings

## Project Structure

```
LtiRom/
├── .idea/                    # IntelliJ configuration files
│   ├── runConfigurations/    # Run configurations
│   ├── compiler.xml         # Compiler settings
│   ├── gradle.xml           # Gradle integration
│   └── misc.xml             # Project settings
├── composeApp/              # Main application module
│   ├── src/jvmMain/kotlin/  # JVM-specific code
│   └── build.gradle.kts     # Module build script
├── build.gradle.kts         # Root build script
└── gradle/                  # Gradle wrapper
```

## Additional Tips

1. **Code Completion**: IntelliJ provides excellent Kotlin and Compose code completion
2. **Live Templates**: Use `main` + Tab for main function template
3. **Refactoring**: Right-click for rename, extract method, etc.
4. **Version Control**: Git integration is configured in `.idea/vcs.xml`
5. **Code Style**: Kotlin official style is configured

## Getting Help

- Check the main README.md for application features
- IntelliJ Help: Help → Help Topics
- Kotlin Multiplatform documentation
- Compose Multiplatform documentation
