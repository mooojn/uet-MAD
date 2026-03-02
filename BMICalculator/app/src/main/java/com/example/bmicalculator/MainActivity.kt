package com.example.bmicalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameInput = findViewById(R.id.etName)
        ageInput = findViewById(R.id.etAge)
        heightInput = findViewById(R.id.etHeight)
        weightInput = findViewById(R.id.etWeight)
        val showButton: Button = findViewById(R.id.btnShowResult)

        showButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val age = ageInput.text.toString().trim().toIntOrNull()
            val heightCm = heightInput.text.toString().trim().toDoubleOrNull()
            val weightKg = weightInput.text.toString().trim().toDoubleOrNull()

            if (!validateInputs(name, age, heightCm, weightKg)) return@setOnClickListener

            val resultIntent = Intent(this, ResultActivity::class.java).apply {
                putExtra(ResultActivity.EXTRA_NAME, name)
                putExtra(ResultActivity.EXTRA_AGE, age)
                putExtra(ResultActivity.EXTRA_HEIGHT_CM, heightCm)
                putExtra(ResultActivity.EXTRA_WEIGHT_KG, weightKg)
            }
            startActivity(resultIntent)
        }
    }

    private fun validateInputs(
        name: String,
        age: Int?,
        heightCm: Double?,
        weightKg: Double?
    ): Boolean {
        nameInput.error = null
        ageInput.error = null
        heightInput.error = null
        weightInput.error = null

        var isValid = true

        if (name.isEmpty()) {
            nameInput.error = getString(R.string.error_name_required)
            isValid = false
        }
        if (age == null || age <= 0) {
            ageInput.error = getString(R.string.error_valid_age)
            isValid = false
        }
        if (heightCm == null || heightCm <= 0.0) {
            heightInput.error = getString(R.string.error_valid_height)
            isValid = false
        }
        if (weightKg == null || weightKg <= 0.0) {
            weightInput.error = getString(R.string.error_valid_weight)
            isValid = false
        }
        return isValid
    }
}
