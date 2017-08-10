package com.shakuro.skylocker

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_kotlin.*

class KotlinActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)
        this.button.setOnClickListener {
            startActivity(Intent(this, JavaActivity::class.java))
        }
    }

}
