package com.shakuro.skylocker.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.SkyLockerManager
import com.shakuro.skylocker.model.entities.DaoMaster
import com.shakuro.skylocker.model.entities.DaoSession
import com.shakuro.skylocker.model.skyeng.SkyEngApi
import dagger.Module
import dagger.Provides
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class ForSkyLockerDB

@Module(includes = arrayOf(SkyEngApiModule::class, ContextModule::class))
class SkyLockerManagerModule {

    @Provides
    @Singleton
    fun provideSkyLockerManager(skyEngApi: SkyEngApi,
                                preferences: SharedPreferences,
                                daoSession: DaoSession): SkyLockerManager {
        return SkyLockerManager(skyEngApi, preferences, daoSession)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ForApplication context: Context) =
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideDaoSession(@ForApplication context: Context,
                          @ForSkyLockerDB dbFile: File): DaoSession {
        if (!dbFile.exists()) {
            val inputStream = context.resources.openRawResource(R.raw.skylockerdb)
            val outputStream = FileOutputStream(dbFile)
            IOUtils.copy(inputStream, outputStream)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(outputStream)
        }
        val db = DaoMaster.DevOpenHelper(context, dbFile.absolutePath).writableDb
        return DaoMaster(db).newSession()
    }

    @Provides
    @Singleton
    @ForSkyLockerDB
    fun provideBlurredImageFile(@ForApplication context: Context): File {
        return File(context.filesDir, SkyLockerManager.DB_FILE_NAME)
    }

}