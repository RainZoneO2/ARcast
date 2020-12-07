package com.rain.arcast

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginBtn: Button = findViewById(R.id.btn_signin)
        val backBtn: Button = findViewById(R.id.btn_back)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        auth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener {
            loginUser()
        }

        backBtn.setOnClickListener {
            startActivity(Intent(this, SplashScreen::class.java))
            //finish()
        }
    }

    private fun loginUser() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        if (etEmail.text.toString().isEmpty() || etPassword.text.toString().isEmpty())
            Toast.makeText(
                applicationContext,
                "Email or Password isn't provided",
                Toast.LENGTH_SHORT
            ).show()
        else {
            auth.signInWithEmailAndPassword(
                etEmail.text.toString(),
                etPassword.text.toString()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Failed! Error: " + task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI(null)
                    }
                }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        //TODO: Fix backstack not clearing on finish()
        if (currentUser != null) {
            intent = Intent(this@LoginActivity, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
    }

}