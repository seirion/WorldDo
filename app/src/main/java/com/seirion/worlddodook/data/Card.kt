package com.seirion.worlddodook.data

import android.util.Log
import android.widget.TextView
import com.seirion.worlddodook.getPriceInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.concurrent.TimeUnit



class Card(var code: String, val price: TextView, val hour: TextView, val min: TextView) {

    private var disposable: Disposable? = null

    fun start() {
        trigger()
    }

    fun dispose() {
        disposable?.dispose()
        disposable = null
    }

    fun resume(code: String) {
        this.code = code
        dispose()
        start()
    }

    private fun trigger() {
        disposable = Observable.interval(30, TimeUnit.SECONDS).startWith(0)
                .map { getPriceInfo(code) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updateUi(it) }, { Log.e(TAG, "error : $it", it) })
    }

    private fun updateUi(value: Int) {
        val date = GregorianCalendar()
        hour.text = String.format(Locale.US, "%02d", date.get(Calendar.HOUR))
        min.text = String.format(Locale.US, "%02d", date.get(Calendar.MINUTE))
        price.text = value.toString()
    }

    companion object {
        private const val TAG = "Log"
    }
}
