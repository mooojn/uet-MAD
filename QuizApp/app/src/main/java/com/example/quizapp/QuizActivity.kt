package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private val questions = listOf(
        Question(
            questionText = "Q1. What is 12 + 8?",
            options = listOf("18", "20", "22", "15"),
            correctAnswerIndex = 1
        ),
        Question(
            questionText = "Q2. What is 15 × 3?",
            options = listOf("30", "50", "45", "35"),
            correctAnswerIndex = 2
        ),
        Question(
            questionText = "Q3. What is 100 ÷ 4?",
            options = listOf("20", "30", "25", "50"),
            correctAnswerIndex = 2
        ),
        Question(
            questionText = "Q4. What is 56 − 29?",
            options = listOf("37", "25", "17", "27"),
            correctAnswerIndex = 3
        ),
        Question(
            questionText = "Q5. What is 9 × 9?",
            options = listOf("72", "81", "90", "99"),
            correctAnswerIndex = 1
        )
    )

    private lateinit var radioGroups: List<RadioGroup>
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        btnSubmit = findViewById(R.id.btnSubmit)

        // Get references to all question TextViews and RadioGroups
        val questionTextViews = listOf<TextView>(
            findViewById(R.id.tvQuestion1),
            findViewById(R.id.tvQuestion2),
            findViewById(R.id.tvQuestion3),
            findViewById(R.id.tvQuestion4),
            findViewById(R.id.tvQuestion5)
        )

        radioGroups = listOf(
            findViewById(R.id.rgQuestion1),
            findViewById(R.id.rgQuestion2),
            findViewById(R.id.rgQuestion3),
            findViewById(R.id.rgQuestion4),
            findViewById(R.id.rgQuestion5)
        )

        val radioButtonIds = listOf(
            listOf(R.id.rb1_option1, R.id.rb1_option2, R.id.rb1_option3, R.id.rb1_option4),
            listOf(R.id.rb2_option1, R.id.rb2_option2, R.id.rb2_option3, R.id.rb2_option4),
            listOf(R.id.rb3_option1, R.id.rb3_option2, R.id.rb3_option3, R.id.rb3_option4),
            listOf(R.id.rb4_option1, R.id.rb4_option2, R.id.rb4_option3, R.id.rb4_option4),
            listOf(R.id.rb5_option1, R.id.rb5_option2, R.id.rb5_option3, R.id.rb5_option4)
        )

        // Populate questions and options
        for (i in questions.indices) {
            questionTextViews[i].text = questions[i].questionText

            for (j in 0 until 4) {
                val radioButton = findViewById<RadioButton>(radioButtonIds[i][j])
                radioButton.text = questions[i].options[j]
            }
        }

        // Listen for changes on each RadioGroup to enable submit when all answered
        for (rg in radioGroups) {
            rg.setOnCheckedChangeListener { _, _ ->
                checkAllAnswered()
            }
        }

        // Submit button click
        btnSubmit.setOnClickListener {
            val score = calculateScore()
            val intent = Intent(this, ScoreActivity::class.java)
            intent.putExtra("SCORE", score)
            intent.putExtra("TOTAL", questions.size)
            startActivity(intent)
            finish()
        }
    }

    private fun checkAllAnswered() {
        val allAnswered = radioGroups.all { it.checkedRadioButtonId != -1 }
        btnSubmit.isEnabled = allAnswered
    }

    private fun calculateScore(): Int {
        var score = 0

        val radioButtonIds = listOf(
            listOf(R.id.rb1_option1, R.id.rb1_option2, R.id.rb1_option3, R.id.rb1_option4),
            listOf(R.id.rb2_option1, R.id.rb2_option2, R.id.rb2_option3, R.id.rb2_option4),
            listOf(R.id.rb3_option1, R.id.rb3_option2, R.id.rb3_option3, R.id.rb3_option4),
            listOf(R.id.rb4_option1, R.id.rb4_option2, R.id.rb4_option3, R.id.rb4_option4),
            listOf(R.id.rb5_option1, R.id.rb5_option2, R.id.rb5_option3, R.id.rb5_option4)
        )

        for (i in questions.indices) {
            val selectedId = radioGroups[i].checkedRadioButtonId
            val correctId = radioButtonIds[i][questions[i].correctAnswerIndex]
            if (selectedId == correctId) {
                score++
            }
        }

        return score
    }
}
