package com.trueedu.world.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.trueedu.world.getPriceInfo
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object DataSource {

    private const val TAG = "DataSource"

    private var appContext: Context? = null
    private var source: BehaviorSubject<List<PriceInfo>> = BehaviorSubject.create()
    private var disposable: Disposable? = null
    private var codes: ArrayList<String>? = null
    var updateTime: Date? = null // time when last updated

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
        val coolTime = Settings.coolTimeSec
        disposable = Observable.interval(coolTime, TimeUnit.SECONDS).startWith(0)
                .map { getPriceInfo(codes!!) }
                .subscribeOn(Schedulers.io())
                .doOnNext { updateTime = Calendar.getInstance().time }
                .subscribe({ source.onNext(it) }, { Log.e(TAG, "error : ", it) })
    }

    fun stop() {
        disposable?.dispose()
    }

    fun restart() {
        stop()
        start()
    }

    fun clear() {
        if (source.hasObservers()) {
            source.onComplete()
        }
        source = BehaviorSubject.create()
    }

    fun getLatest() =
            (if (source.hasValue()) {
                source.value
            } else {
                Collections.emptyList<PriceInfo>()
            })!!
}
