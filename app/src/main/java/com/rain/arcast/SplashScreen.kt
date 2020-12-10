package com.rain.arcast

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val loginBtn: Button = findViewById(R.id.btn_loginSplash)
        val registerBtn: Button = findViewById(R.id.btn_registerSplash)

        loginBtn.setOnClickListener {
            Log.d("Splash", "Login button")
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerBtn.setOnClickListener {
            Log.d("Splash", "Register button")
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val sl = AnimationUtils.loadAnimation(this, R.anim.sl)
        val btnsl = AnimationUtils.loadAnimation(this, R.anim.btnsl)
        val icon = findViewById<ImageView>(R.id.ivLogoSplash)
        icon.startAnimation(sl)
        loginBtn.startAnimation(btnsl)
        registerBtn.startAnimation(btnsl)
    }


}