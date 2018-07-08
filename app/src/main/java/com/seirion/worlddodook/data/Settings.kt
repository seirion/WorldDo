package com.seirion.worlddodook.data

import android.annotation.SuppressLint
import android.content.Context
import rx.subjects.BehaviorSubject
import rx.Observable

@SuppressLint("StaticFieldLeak")
object Settings {

    private const val PREFS_CODE_NUM = "PREFS_CODE_NUM"
    private const val PREFS_COOL_TIME = "PREFS_COOL_TIME"

    private lateinit var appContext: Context
    private var settingChanges: BehaviorSubject<Int> = BehaviorSubject.create()

    const val MAX_CODE_NUM = 5

    var codeNum = 1
        set(value) {
            if (field != value) {
                field = value
                settingChanges.onNext(field)
                appContext.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                        .putInt(PREFS_CODE_NUM, codeNum)
                        .apply()
            }
        }

    var coolTimeSec = 10L
        set(value) {
            if (field != value) {
                field = value
                DataSource.restart()
                appContext.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                        .putLong(PREFS_COOL_TIME, field)
                        .apply()
            }
        }

    fun init(appContext: Context) {
        this.appContext = appContext
        val prefs = appContext.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE)
        codeNum = prefs.getInt(PREFS_CODE_NUM, codeNum)
    }

    fun observeSettingChanges(): Observable<Int> {
        return settingChanges
    }
}
