package com.shakuro.skylocker.di.modules

import android.content.Context
import com.shakuro.skylocker.system.RingStateManager
import dagger.Module
import dagger.Provides
import ru.terrakok.gitlabclient.model.system.ResourceManager
import javax.inject.Singleton

@Module(includes = arrayOf(ContextModule::class))
class SystemServicesModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context) =
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideResourceManager(@ApplicationContext context: Context): ResourceManager {
        return ResourceManager(context)
    }

    @Provides
    @Singleton
    fun provideRingStateManager(@ApplicationContext context: Context): RingStateManager {
        return RingStateManager(context)
    }
}