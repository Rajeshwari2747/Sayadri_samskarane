package com.sahyadri.samrakshane

import android.app.Activity
import android.content.Intent

fun Activity.launchWithSlide(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun Activity.finishWithSlide() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

