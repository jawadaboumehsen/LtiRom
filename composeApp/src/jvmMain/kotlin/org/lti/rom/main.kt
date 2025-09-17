package org.lti.rom

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.lti.rom.di.appModule

fun main() {
    startKoin {
        modules(appModule)
    }
    application {
        Napier.base(DebugAntilog())
        Window(
            onCloseRequest = ::exitApplication,
            title = "LtiRom",
        ) {
            App()
        }
    }
}