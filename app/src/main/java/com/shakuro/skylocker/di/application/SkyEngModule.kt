package com.shakuro.skylocker.di.application

import com.shakuro.skylocker.model.skyeng.SkyEngDictionaryApi
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.SkyEngUserApi
import com.shakuro.skylocker.model.skyeng.models.db.DaoSession
import toothpick.config.Module

class SkyEngModule() : Module() {
    init {
        bind(SkyEngDictionaryApi::class.java).toInstance(SkyEngDictionaryApi.create())
        bind(SkyEngUserApi::class.java).toInstance(SkyEngUserApi.create())
        bind(DaoSession::class.java).toProvider(DBProvider::class.java).singletonInScope()
        bind(SkyEngRepository::class.java).singletonInScope()
    }
}