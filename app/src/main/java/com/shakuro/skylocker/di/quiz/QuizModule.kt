package com.shakuro.skylocker.di.quiz

import android.content.Context
import com.shakuro.skylocker.di.application.ApplicationContext
import com.shakuro.skylocker.model.quiz.QuizBgImageLoader
import com.shakuro.skylocker.model.quiz.QuizInteractor
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import dagger.Module
import dagger.Provides
import java.io.File

@Module
@QuizScope
class QuizModule {

    @Provides
    @QuizScope
    fun provideQuizInteractor(skyEngRepository: SkyEngRepository, settingsRepository: SettingsRepository):QuizInteractor {
        return QuizInteractor(skyEngRepository, settingsRepository)
    }

    @Provides
    @QuizScope
    fun provideQuizBgImageLoader(@ApplicationContext context: Context, @QuizBgImageFile file: File): QuizBgImageLoader {
        return QuizBgImageLoader(context, file)
    }

    @Provides
    @QuizScope
    @QuizBgImageFile
    fun provideQuizBgImageFile(@ApplicationContext context: Context): File {
        return File(context.filesDir, QuizBgImageLoader.FILE_NAME)
    }
}