package com.example.studylog

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000 // 3초 동안 스플래시 화면 표시

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed ({
            // 스플래시 화면이 표시된 후 실행될 코드
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 스플래시 화면 액티비티 종료
        }, SPLASH_TIME_OUT)
    }
}
