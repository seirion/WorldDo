package com.seirion.worlddodook.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.seirion.worlddodook.getPriceInfo
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object DataSource {

    private const val TAG = "DataSource"
    private const val DEFAULT_PREFS = "DEFAULT_PREFS"
    private const val PREFS_DEFAULT_KEY_CODE = "PREFS_DEFAULT_KEY_CODE"
    private const val DEFAULT_KEY_CODE = "043710" // default ㅅㅇㄹㄱ
    private const val EMISSION_COOL_TIME_MS = 10000

    private var appContext: Context? = null
    private val source: BehaviorSubject<List<PriceInfo>> = BehaviorSubject.create()
    private var disposable: Disposable? = null

    fun set(code: String) {
        appContext?.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE)!!.edit()
                .putString(PREFS_DEFAULT_KEY_CODE, code)
                .apply()

        restart()
    }

    fun observeChanges(): Observable<List<PriceInfo>> {
        return source
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun start() {
        val code = codeSaved()
        disposable = Observable.interval(EMISSION_COOL_TIME_MS, TimeUnit.MILLISECONDS).startWith(0)
                // TODO: Query multiple stocks
                .map { getPriceInfo(listOf(code)) }
                .subscribeOn(Schedulers.io())
                .subscribe({ source.onNext(it) }, { Log.e(TAG, "error : ", it) })
    }

    fun stop() {
        disposable?.dispose()
    }

    private fun restart() {
        stop()
        start()
    }

    fun clear() {
        if (source.hasObservers()) {
            source.onComplete()
        }
    }

    private fun codeSaved(): String {
        val prefs = appContext?.getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE)
        return prefs?.getString(PREFS_DEFAULT_KEY_CODE, DEFAULT_KEY_CODE) ?: DEFAULT_KEY_CODE
    }
}
