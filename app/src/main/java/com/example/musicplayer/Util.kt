package com.example.musicplayer

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*


fun ImageView.scaleFromZero(icon : Bitmap){
    setImageBitmap(icon)
    val scale = ScaleAnimation(0f, 1f, 0f, 1f, 500f, 400f)
    scale.duration = 500
    scale.startOffset = 100
    startAnimation(scale)
}

fun ImageView.translateToLeft(){
    val translate = TranslateAnimation(0f, -1000f, 0f, 0f)
    translate.duration = 500
    translate.setAnimationListener(object : Animation.AnimationListener{
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            visibility = View.INVISIBLE
        }
        override fun onAnimationStart(animation: Animation?) {
        }
    })
    startAnimation(translate)
}

fun ImageView.translateArrowButton(value : Float){
    val animator = ObjectAnimator.ofFloat(this, "translationY", value)
    animator.duration = 500
    animator.repeatCount = 1
    animator.repeatMode = ValueAnimator.REVERSE
    animator.start()
}

fun ImageView.rotate(){
    val rotate = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f)
    rotate.duration = 1000
    rotate.start()
}