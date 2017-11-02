package com.shakuro.skylocker.di.modules

import android.content.Context
import com.shakuro.skylocker.model.quiz.QuizBgImageLoader
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module(includes = arrayOf(ContextModule::class))
class QuizModule {

    @Provides
    @Singleton
    fun provideQuizBgImageLoader(@ApplicationContext context: Context, @QuizBgImageFile file: File): QuizBgImageLoader {
        return QuizBgImageLoader(context, file)
    }

    @Provides
    @Singleton
    @QuizBgImageFile
    fun provideQuizBgImageFile(@ApplicationContext context: Context): File {
        return File(context.filesDir, QuizBgImageLoader.FILE_NAME)
    }
}