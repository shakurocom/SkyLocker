package com.shakuro.skylocker.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.quiz.QuizInteractor
import com.shakuro.skylocker.model.settings.SettingsInteractor
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.model.skyeng.SkyEngDictionaryApi
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.SkyEngUserApi
import com.shakuro.skylocker.model.skyeng.models.db.DaoMaster
import com.shakuro.skylocker.model.skyeng.models.db.DaoSession
import com.shakuro.skylocker.system.LockServiceManager
import dagger.Module
import dagger.Provides
import org.apache.commons.io.IOUtils
import ru.terrakok.gitlabclient.model.system.ResourceManager
import java.io.File
import java.io.FileOutputStream
import javax.inject.Singleton

@Module(includes = arrayOf(SkyEngApiModule::class, ContextModule::class, SystemServicesModule::class))
class SkyLockerManagerModule {

    @Provides
    @Singleton
    fun provideSkyLockerManager(skyEngDictionaryApi: SkyEngDictionaryApi,
                                skyEngUserApi: SkyEngUserApi,
                                settingsRepository: SettingsRepository,
                                daoSession: DaoSession): SkyEngRepository {
        return SkyEngRepository(skyEngDictionaryApi, skyEngUserApi, settingsRepository, daoSession)
    }

    @Provides
    @Singleton
    fun provideDaoSession(@ApplicationContext context: Context,
                          @SkyLockerDBFile dbFile: File): DaoSession {
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
    @SkyLockerDBFile
    fun provideSkyLockerDBFile(@ApplicationContext context: Context): File {
        return File(context.filesDir, SkyEngRepository.DB_FILE_NAME)
    }

    @Provides
    @Singleton
    fun provideLockServiceManager(@ApplicationContext context: Context) = LockServiceManager(context)

    @Provides
    @Singleton
    fun provideSettingsInteractor(slManager: SkyEngRepository, lockServiceManager: LockServiceManager, sr: SettingsRepository, rm: ResourceManager) =
        SettingsInteractor(slManager, lockServiceManager, sr, rm)

    @Provides
    @Singleton
    fun provideQuizInteractor(skyEngRepository: SkyEngRepository, sr: SettingsRepository) = QuizInteractor(skyEngRepository, sr)

    @Provides
    @Singleton
    fun provideSettingsRepository(preferences: SharedPreferences)= SettingsRepository(preferences)
}