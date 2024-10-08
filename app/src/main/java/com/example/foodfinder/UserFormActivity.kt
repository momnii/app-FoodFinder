package com.example.foodfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodfinder.R

class UserFormActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var age: EditText
    private lateinit var emailName: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_form)

        username = findViewById(R.id.username)
        age = findViewById(R.id.age)
        emailName = findViewById(R.id.email_name)
        submitButton = findViewById(R.id.submit_button)

        submitButton.setOnClickListener {
            val name = username.text.toString()
            val userAge = age.text.toString()
            val userEmailName = emailName.text.toString()

            if (isInputValid(name, userAge, userEmailName)) {
                val intent = Intent(this, DashboardActivity::class.java).apply {
                    putExtra("USER_NAME", name) // Benutzername an DashboardActivity übergeben
                    putExtra("USER_AGE", userAge)
                    putExtra("USER_EMAIL_NAME", userEmailName)
                }
                startActivity(intent)
            }
        }
    }
    private fun isInputValid(name: String, age: String, email: String): Boolean {
        if (name.isEmpty()) {
            showToast("Bitte geben Sie Ihren Benutzernamen ein.")
            return false
        }
        if (age.isEmpty()) {
            showToast("Bitte geben Sie Ihr Alter ein.")
            return false
        }
        if (email.isEmpty()) {
            showToast("Bitte geben Sie Ihre E-Mail-Adresse ein.")
            return false
        }
        if (!isValidEmail(email)) {
            showToast("Bitte geben Sie eine gültige E-Mail-Adresse ein.")
            return false
        }
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        // Einfache Regex für die E-Mail-Validierung
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


