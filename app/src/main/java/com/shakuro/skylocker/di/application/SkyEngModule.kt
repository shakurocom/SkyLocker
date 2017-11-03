package com.shakuro.skylocker.di.application

import android.content.Context
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.model.skyeng.SkyEngDictionaryApi
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.SkyEngUserApi
import com.shakuro.skylocker.model.skyeng.models.db.DaoMaster
import com.shakuro.skylocker.model.skyeng.models.db.DaoSession
import dagger.Module
import dagger.Provides
import org.apache.commons.io.IOUtils
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import javax.inject.Singleton

@Module
class SkyEngModule {

    @Provides
    @Singleton
    fun provideSkyEngRepository(skyEngDictionaryApi: SkyEngDictionaryApi,
                                skyEngUserApi: SkyEngUserApi,
                                settingsRepository: SettingsRepository,
                                daoSession: DaoSession): SkyEngRepository {
        return SkyEngRepository(skyEngDictionaryApi, skyEngUserApi, settingsRepository, daoSession)
    }

    @Provides
    @Singleton
    fun provideSkyEngDictionaryApi(builder: Retrofit.Builder, converterFactory: Converter.Factory) =
            SkyEngDictionaryApi.create(builder, converterFactory)

    @Provides
    @Singleton
    fun provideSkyEngUserApi(builder: Retrofit.Builder, converterFactory: Converter.Factory) =
            SkyEngUserApi.create(builder, converterFactory)

    @Provides
    @Singleton
    fun provideRetrofitBuilder() = Retrofit.Builder()

    @Provides
    @Singleton
    fun provideConverterFactory(): Converter.Factory = MoshiConverterFactory.create()

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
}