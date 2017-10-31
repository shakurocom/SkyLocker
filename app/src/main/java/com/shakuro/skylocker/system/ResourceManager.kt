package ru.terrakok.gitlabclient.model.system

import android.content.Context

class ResourceManager constructor(private val context: Context) {

    fun getString(id: Int) = context.getString(id)
}