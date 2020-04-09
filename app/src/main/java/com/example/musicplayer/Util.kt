package com.example.musicplayer

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView

fun ImageView.scaleFromZero(){
    ScaleAnimation(0f, 1f, 0f, 1f, 500f, 400f).apply {
        duration = DURATION_SHORT
        startOffset = START_OFFSET
        startAnimation(this)
    }
}

fun ImageView.translateToLeft(){
    TranslateAnimation(0f, -1000f, 0f, 0f).apply {
        duration = DURATION_SHORT
        setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.INVISIBLE
                setImageDrawable(null)
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })
        startAnimation(this)
    }
}

fun ImageView.translateArrowButton(value : Float){
    ObjectAnimator.ofFloat(this, ImageView.TRANSLATION_Y, value).apply {
        duration = DURATION_SHORT
        repeatCount = REPEAT_COUNT
        repeatMode = ValueAnimator.REVERSE
        start()
    }
}

fun ImageView.rotate(){
    ObjectAnimator.ofFloat(this, ImageView.ROTATION, 0f, 360f).apply {
        duration = DURATION_LONG
        start()
    }
}

private const val DURATION_SHORT = 500L
private const val DURATION_LONG = 1000L
private const val START_OFFSET = 100L
private const val REPEAT_COUNT = 1