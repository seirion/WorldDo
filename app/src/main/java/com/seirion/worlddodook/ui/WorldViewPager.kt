package com.seirion.worlddodook.ui

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class WorldViewPager : ViewPager {
    companion object {
        const val MOVE_SENSITIVITY = 10f
    }

    private var initialXValue = 0f
    private var blockLeftSwiping = false
    private var blockRightSwiping = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun blockLeft(block: Boolean) {
        this.blockLeftSwiping = block
    }

    fun blockRight(block: Boolean) {
        this.blockRightSwiping = block
    }

    private fun rightMoving(event: MotionEvent) = !leftMoving(event)

    private fun leftMoving(event: MotionEvent): Boolean {
        try {
            val diffX = event.x - initialXValue
            if (diffX > MOVE_SENSITIVITY) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (isIgnoreEvent(event)) {
            false
        } else super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isIgnoreEvent(event)) {
            return false
        }
        return super.onTouchEvent(event)
    }

    private fun isIgnoreEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialXValue = event.x
                false
            }
            MotionEvent.ACTION_MOVE -> {
                (blockLeftSwiping && leftMoving(event)) || (blockRightSwiping && rightMoving(event))
            }
            else -> false
        }
    }
}
