package com.shakuro.skylocker.model.skyeng.models.skyeng

data class SkyEngWord(
        val id: Long,
        val text: String,
        val meanings: MutableList<SkyEngMeaning>)