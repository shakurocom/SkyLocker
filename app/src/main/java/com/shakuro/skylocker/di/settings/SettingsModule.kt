package com.shakuro.skylocker.di.settings

import com.shakuro.skylocker.model.settings.SettingsInteractor
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.system.LockServiceManager
import dagger.Module
import dagger.Provides
import ru.terrakok.gitlabclient.model.system.ResourceManager

@Module
@SettingsScope
class SettingsModule {

    @Provides
    @SettingsScope
    fun provideSettingsInteractor(skyEngRepository: SkyEngRepository,
                                  lockServiceManager: LockServiceManager,
                                  settingsRepository: SettingsRepository,
                                  resourceManager: ResourceManager): SettingsInteractor {
        return SettingsInteractor(skyEngRepository, lockServiceManager, settingsRepository, resourceManager)
    }
}