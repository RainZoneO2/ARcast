package com.rain.arcast

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var loginBtn : Button = findViewById(R.id.btn_login)
        var registerBtn : Button = findViewById(R.id.btn_register)

        loginBtn.setOnClickListener {
            Log.d("Splash", "Login button")
            startActivity(Intent(this, MainActivity::class.java))
        }

        registerBtn.setOnClickListener {
            Log.d("Splash", "Register button")
            //startActivity(Intent(this, RegisterDialog::class.java))
        }
    }

}