package com.trueedu.world.data

import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

@SuppressLint("StaticFieldLeak")
object Settings {

    private const val PREFS_CODE_NUM = "PREFS_CODE_NUM"
    private const val PREFS_COOL_TIME = "PREFS_COOL_TIME"
    private const val PREFS_CURRENT_PAGE = "PREFS_CURRENT_PAGE"

    private lateinit var appContext: Context
    private var settingChanges: BehaviorSubject<Int> = BehaviorSubject.create()

    const val MIN_CODE_NUM = 1
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

    const val MIN_COOL_TIME_SEC = 3L
    const val MAX_COOL_TIME_SEC = 30L

    var coolTimeSec = 10L
        set(value) {
            if (field != value) {
                field = value
                appContext.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                        .putLong(PREFS_COOL_TIME, field)
                        .apply()
            }
        }

    var currentPage = 0
        set(value) {
            if (field != value) {
                field = value
                appContext.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                        .putInt(PREFS_CURRENT_PAGE, field)
                        .apply()
            }
        }

    fun init(appContext: Context) {
        this.appContext = appContext
        val prefs = appContext.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE)
        codeNum = prefs.getInt(PREFS_CODE_NUM, codeNum)
        coolTimeSec = prefs.getLong(PREFS_COOL_TIME, coolTimeSec)
        currentPage = prefs.getInt(PREFS_CURRENT_PAGE, currentPage)
    }

    fun observeSettingChanges(): Observable<Int> {
        return settingChanges
    }
}
