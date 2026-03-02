package com.example.bmicalculator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val age = intent.getIntExtra(EXTRA_AGE, -1)
        val heightCm = intent.getDoubleExtra(EXTRA_HEIGHT_CM, -1.0)
        val weightKg = intent.getDoubleExtra(EXTRA_WEIGHT_KG, -1.0)

        if (name.isBlank() || age <= 0 || heightCm <= 0.0 || weightKg <= 0.0) {
            finish()
            return
        }

        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)

        val (categoryText, categoryColorRes) = when {
            bmi < 18.5 -> getString(R.string.bmi_underweight) to R.color.bmi_underweight_blue
            bmi < 25.0 -> getString(R.string.bmi_normal) to R.color.bmi_normal_green
            else -> getString(R.string.bmi_overweight) to R.color.bmi_overweight_red
        }

        findViewById<TextView>(R.id.tvName).text = getString(R.string.label_name_value, name)
        findViewById<TextView>(R.id.tvAge).text = getString(R.string.label_age_value, age)
        findViewById<TextView>(R.id.tvHeight).text =
            getString(R.string.label_height_value, formatNumber(heightCm))
        findViewById<TextView>(R.id.tvWeight).text =
            getString(R.string.label_weight_value, formatNumber(weightKg))
        findViewById<TextView>(R.id.tvBmiValue).text = formatNumber(bmi)

        val categoryView = findViewById<TextView>(R.id.tvBmiCategory)
        categoryView.text = categoryText
        categoryView.setTextColor(ContextCompat.getColor(this, categoryColorRes))
    }

    private fun formatNumber(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_AGE = "extra_age"
        const val EXTRA_HEIGHT_CM = "extra_height_cm"
        const val EXTRA_WEIGHT_KG = "extra_weight_kg"
    }
}
