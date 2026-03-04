package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ScoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 5)

        val tvEmoji = findViewById<TextView>(R.id.tvEmoji)
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvFeedback = findViewById<TextView>(R.id.tvFeedback)
        val btnRetry = findViewById<Button>(R.id.btnRetry)

        tvScore.text = "$score / $total"

        // Set feedback based on score
        when {
            score == total -> {
                tvEmoji.text = "🏆"
                tvFeedback.text = "Perfect! You're a math genius!"
            }
            score >= total * 0.6 -> {
                tvEmoji.text = "😊"
                tvFeedback.text = "Great job! Keep practicing!"
            }
            score >= total * 0.4 -> {
                tvEmoji.text = "🤔"
                tvFeedback.text = "Not bad! Room for improvement."
            }
            else -> {
                tvEmoji.text = "📚"
                tvFeedback.text = "Keep studying! You'll get better!"
            }
        }

        // Retry button goes back to quiz
        btnRetry.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
