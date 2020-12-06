package com.rain.arcast

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val loginBtn: Button = findViewById(R.id.btn_login)
        val registerBtn: Button = findViewById(R.id.btn_register)
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etPassword: EditText = findViewById(R.id.etPassword)

        auth = FirebaseAuth.getInstance()
        loginBtn.setOnClickListener {
            Log.d("Splash", "Login button")
            startActivity(Intent(this, SignInActivity::class.java))
        }

        registerBtn.setOnClickListener {
            Log.d("Splash", "Register button")

            startActivity(Intent(this, RegisterActivity::class.java))
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            if (etEmail.text.toString().isEmpty() || etPassword.text.toString().isEmpty())
                Toast.makeText(
                    applicationContext,
                    "Email or Password isnt provided",
                    Toast.LENGTH_SHORT
                ).show()
            //textViewResponse.text = "Email Address or Password is not provided"
            else {
                auth.createUserWithEmailAndPassword(
                    etEmail.text.toString(),
                    etPassword.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "Sign up successful!",
                                Toast.LENGTH_SHORT
                            ).show()
                            //textViewResponse.text = "Sign Up successfull. Email and Password created"
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Failed! Error: " + task.exception!!.message,
                                Toast.LENGTH_SHORT
                            ).show()


                            //textViewResponse.text = "Sign Up Failed"
                            updateUI(null)
                        }
                    }
            }
            //startActivity(Intent(this, RegisterDialog::class.java))
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        //currentUser.email
    }

}