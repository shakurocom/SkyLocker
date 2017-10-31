package com.shakuro.skylocker.di.modules

import android.content.Context
import com.shakuro.skylocker.ui.BlurredImageLoader
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module(includes = arrayOf(ContextModule::class))
class BlurredImageModule {

    @Provides
    @Singleton
    fun provideBlurredImageLoader(@ApplicationContext context: Context, @BlurredImageFile file: File): BlurredImageLoader {
        return BlurredImageLoader(context, file)
    }

    @Provides
    @Singleton
    @BlurredImageFile
    fun provideBlurredImageFile(@ApplicationContext context: Context): File {
        return File(context.filesDir, BlurredImageLoader.FILE_NAME)
    }
}