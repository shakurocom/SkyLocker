package com.shakuro.skylocker.di.modules

import android.content.Context
import com.shakuro.skylocker.ui.BlurredImageLoader
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class ForBlurredImage

@Module(includes = arrayOf(ContextModule::class))
class BlurredImageModule {

    @Provides
    @Singleton
    fun provideBlurredImageLoader(@ForApplication context: Context, @ForBlurredImage file: File): BlurredImageLoader {
        return BlurredImageLoader(context, file)
    }

    @Provides
    @Singleton
    @ForBlurredImage
    fun provideBlurredImageFile(@ForApplication context: Context): File {
        return File(context.filesDir, BlurredImageLoader.FILE_NAME)
    }
}