package com.seirion.worlddodook.data

import android.util.Log
import android.widget.TextView
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale


val moshi: Moshi = Moshi.Builder().build()
val adapter = NaverRealTimeResponse.jsonAdapter(moshi)

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
                .map { getPriceInfo() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updateUi(it) }, { Log.e(TAG, "error : $it") })
    }

    private fun updateUi(value: Int) {
        val date = GregorianCalendar()
        hour.text = String.format(Locale.US, "%02d", date.get(Calendar.HOUR))
        min.text = String.format(Locale.US, "%02d", date.get(Calendar.MINUTE))
        price.text = value.toString()
    }

    private fun getPriceInfo(): Int {
        val client = OkHttpClient()
        val request = Request.Builder()
                .url(BASE_URL + code)
                .build()
        val response = client.newCall(request).execute()
        val jsonString = response.body()?.string()
        Log.d(TAG, "log : $jsonString")
        val priceInfo = getPriceInfoOf(jsonString ?: "")
        return priceInfo.current
    }

    private fun getPriceInfoOf(jsonString: String): PriceInfo {
        var data: NaverRealTimeData = DUMMY_DATA
        try {
            val response = adapter.fromJson(jsonString)
            data = response?.result!!.areas[0].datas[0]
        } catch (e: JsonEncodingException) {
            Log.e(TAG, "Fail to parse json: $e", e)
        }
        return PriceInfo(data.code, data.name, data.low, data.high, data.open, data.current)
    }

    companion object {
        private const val TAG = "Log"
        private const val BASE_URL = "http://polling.finance.naver.com/api/realtime.nhn?query=SERVICE_ITEM:"
    }
}
