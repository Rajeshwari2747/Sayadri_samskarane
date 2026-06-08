package com.sahyadri.samrakshane

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EducationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_education)

        window.decorView.alpha = 0f
        window.decorView.animate().alpha(1f).setDuration(240).start()
    }
}