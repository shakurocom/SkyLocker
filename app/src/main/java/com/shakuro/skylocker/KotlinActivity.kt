package com.shakuro.skylocker

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.shakuro.skylocker.model.SkyLockerManager
import com.shakuro.skylocker.model.entities.Meaning
import kotlinx.android.synthetic.main.activity_kotlin.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.util.*


class KotlinActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)

        if (showRandomWord() == null) {
            button.visibility = View.VISIBLE
            button.setOnClickListener {
                SkyLockerManager.instance.fillWithWords(words()) {
                    runOnUiThread() {
                        button.visibility = View.INVISIBLE
                        showRandomWord()
                    }
                }
            }
        }
    }

    private fun showRandomWord(): Meaning? {
        this.flowLayout.removeAllViews()

        val meaning = SkyLockerManager.instance.randomMeaning()
        meaning?.let {
            wordTextView.setText(it.translation)
            definitionTextView.setText(it.definition)

            val answers = SkyLockerManager.instance.answerWithAlternatives(it)
            answers.forEach {
                val button = Button(this)
                button.setText(it)
                this.flowLayout.addView(button)
                if (it == meaning.text) {
                    button.setOnClickListener {
                        Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                        showRandomWord()
                    }
                }
            }
        }
        return meaning
    }

    private fun words(): List<String> {
        val stream = resources.openRawResource(R.raw.test)
        val rdr = BufferedReader(InputStreamReader(stream))
        var line = rdr.readLine()
        val words = mutableListOf<String>()
        while (line != null) {
            line = rdr.readLine()
            if ((line?.length ?: 0) > 0) {
                words.add(line)
            }
        }
        rdr.close()
        stream.close()
        return words
    }
}