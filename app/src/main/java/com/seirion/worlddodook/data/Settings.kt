package com.seirion.worlddodook.data

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object Settings {

    private const val PREFS_CODE_NUM = "PREFS_CODE_NUM"
    private lateinit var appContext: Context
    var codeNum = 1
        set(value) {
            field = value
            appContext.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                    .putInt(PREFS_CODE_NUM, codeNum)
                    .apply()
        }

    fun init(appContext: Context) {
        val prefs = appContext.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE)
        codeNum = prefs.getInt(PREFS_CODE_NUM, codeNum)
    }
}
