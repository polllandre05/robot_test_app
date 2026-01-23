package com.example.robot_test_app_kt

import android.app.Activity
import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

// NO CLASS WRAPPER HERE
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
}

fun ConstraintLayout.animateBias(constraintSet: ConstraintSet, viewId: Int, bias: Float) {
    constraintSet.clone(this)
    constraintSet.setVerticalBias(viewId, bias)
    TransitionManager.beginDelayedTransition(this, AutoTransition().setDuration(600))
    constraintSet.applyTo(this)
}