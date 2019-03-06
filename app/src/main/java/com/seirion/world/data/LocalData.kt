package com.seirion.worlddodook.data

import android.content.Context
import android.util.Log

const val DEFAULT_PREFS = "DEFAULT_PREFS"
private const val TAG = "LocalData"
private const val PREFS_DEFAULT_KEY_CODE = "PREFS_DEFAULT_KEY_CODE"
private const val DEFAULT_KEY_CODE = "043710" // default ㅅㅇㄹㄱ
private const val CODE_DELIMITER = "-"

fun loadCodes(appContext: Context?): ArrayList<String> {
    val prefs = appContext?.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE)
    val codeString = prefs?.getString(PREFS_DEFAULT_KEY_CODE, DEFAULT_KEY_CODE)!!
    return ArrayList(codeString.split(CODE_DELIMITER)).also {
        Log.d(TAG, "load: $codeString")
    }
}

fun saveCodes(appContext: Context?, codes: List<String>) {
    val codeString = codes.joinToString(CODE_DELIMITER)
    Log.d(TAG, codeString)
    appContext?.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE)!!.edit()
            .putString(PREFS_DEFAULT_KEY_CODE, codeString)
            .apply()
}
