package com.shakuro.skylocker.di.modules

import com.shakuro.skylocker.model.skyeng.SkyEngApi
import com.shakuro.skylocker.model.skyeng.SkyEngDictionaryApi
import com.shakuro.skylocker.model.skyeng.SkyEngUserApi
import dagger.Module
import dagger.Provides
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class SkyEngApiModule {

    @Provides
    @Singleton
    fun provideSkyEngApi(dictionaryApi: SkyEngDictionaryApi, userApi: SkyEngUserApi) =
            SkyEngApi(dictionaryApi, userApi)

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
    fun provideConverterFactory() = MoshiConverterFactory.create()
}