package com.shakuro.skylocker.presentation.quiz

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager.LayoutParams
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.shakuro.skylocker.R
import com.shakuro.skylocker.appComponent
import kotlinx.android.synthetic.main.activity_lockscreen.*


class QuizActivity : MvpAppCompatActivity(), QuizView {

    @InjectPresenter
    lateinit var presenter: QuizPresenter

    @ProvidePresenter
    fun providePresenter(): QuizPresenter {
        val presenter = QuizPresenter()
        application.appComponent.inject(presenter)
        return presenter
    }

    private var currentAnswerView: TextView? = null


    // Set appropriate flags to make the screen appear over the keyguard
    override fun onAttachedToWindow() {
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED or LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onAttachedToWindow()
    }
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lockscreen)
        supportActionBar?.hide()
        presenter.checkApplicationWasKilledBySystem(intent)

        skipQuizButton.setOnClickListener {
            presenter.onSkipAction()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        presenter.onSkipAction()
    }

    override fun unlockDevice() {
        finish()
    }

    override fun setBackgroundImage(image: Bitmap) {
        backgroundImageView.setImageBitmap(image)
    }

    override fun setQuizTranslation(translation: String) {
        wordTextView.text = translation
    }

    override fun setQuizDefinition(definition: String) {
        definitionTextView.text = definition
    }

    override fun clearAnswers() {
        flowLayout.removeAllViews()
    }

    override fun addAnswer(answer: Answer) {
        val answerView = LayoutInflater.from(this.flowLayout.context).inflate(R.layout.answer_textview, this.flowLayout, false) as TextView
        answerView.text = answer.text
        answerView.tag = answer
        answerView.setOnClickListener {
            currentAnswerView = answerView
            presenter.checkAnswer(answer)
        }
        this.flowLayout.addView(answerView)
    }

    override fun onAnswerChecked(answer: Answer, right: Boolean) {
        currentAnswerView?.let {
            if (it.tag == answer) {
                val answerBackground = if (right) R.drawable.correct_answer_bg else R.drawable.wrond_answer_bg
                it.setBackgroundResource(answerBackground)
                it.setTextColor(Color.WHITE)
            }
        }
        currentAnswerView = null
    }

    override fun disableControls() {
        skipQuizButton.isEnabled = false
        for (i in 0..flowLayout.childCount - 1) {
            flowLayout.getChildAt(i).isEnabled = false
        }
    }
}