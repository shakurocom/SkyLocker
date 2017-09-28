package com.shakuro.skylocker.di.modules

import com.shakuro.skylocker.model.skyeng.SkyEngApi
import com.shakuro.skylocker.model.skyeng.SkyEngDictionaryApi
import com.shakuro.skylocker.model.skyeng.SkyEngUserApi
import dagger.Module
import dagger.Provides
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
    fun provideSkyEngDictionaryApi(builder: Retrofit.Builder, converterFactory: MoshiConverterFactory): SkyEngDictionaryApi {
        return builder
                .baseUrl(SkyEngDictionaryApi.URL)
                .addConverterFactory(converterFactory)
                .build()
                .create(SkyEngDictionaryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSkyEngUserApi(builder: Retrofit.Builder, converterFactory: MoshiConverterFactory): SkyEngUserApi {
        return builder
                .baseUrl(SkyEngUserApi.URL)
                .addConverterFactory(converterFactory)
                .build()
                .create(SkyEngUserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofitBuilder() = Retrofit.Builder()

    @Provides
    @Singleton
    fun provideMoshiConverterFactory() = MoshiConverterFactory.create()
}