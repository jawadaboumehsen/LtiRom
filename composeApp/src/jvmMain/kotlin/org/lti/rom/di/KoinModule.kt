package org.lti.rom.di

import org.koin.dsl.module
import org.lti.rom.WslService
import org.lti.rom.WslViewModel

val appModule = module {
    single { WslService() }
    factory { WslViewModel(get()) }
}
