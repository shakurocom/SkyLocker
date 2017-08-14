package com.shakuro.skylocker

import android.app.Activity
import android.os.Bundle
import com.shakuro.skylocker.model.SkyLockerManager
import kotlinx.android.synthetic.main.activity_kotlin.*

class KotlinActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)

        this.button.setOnClickListener {
            val words = listOf("celebrate", "encouraging")
            SkyLockerManager.instance.fillWithWords(words)
        }
    }

}
