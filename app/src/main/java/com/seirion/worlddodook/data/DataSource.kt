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
    private const val EMISSION_COOL_TIME_MS = 10000L

    private var appContext: Context? = null
    private val source: BehaviorSubject<List<PriceInfo>> = BehaviorSubject.create()
    private var disposable: Disposable? = null
    private var codes: ArrayList<String>? = null

    fun set(index:Int, code: String) {
        if (codes == null && appContext != null) {
            loadCodes(appContext!!)
        }

        codes?.let {
            if (it.size <= index) {
                it.add(code)
            } else {
                it[index] = code
            }
            saveCodes(appContext, it)
        }
        restart()
    }

    fun get(index: Int): String {
        if (codes == null) {
            codes = loadCodes(appContext!!)
        }
        codes?.let {
            if (index < it.size) {
                return it[index]
            }
        }
        return ""
    }

    fun observeChanges(): Observable<List<PriceInfo>> {
        return source
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun start() {
        if (codes == null) {
            codes = loadCodes(appContext!!)
        }
        disposable = Observable.interval(EMISSION_COOL_TIME_MS, TimeUnit.MILLISECONDS).startWith(0)
                .map { getPriceInfo(codes!!) }
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
}
