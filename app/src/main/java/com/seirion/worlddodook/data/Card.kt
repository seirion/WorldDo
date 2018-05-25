package com.seirion.worlddodook.data

import android.util.Log
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.ParseException
import java.util.concurrent.TimeUnit
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class Card(val code: String, val price: TextView, val hour: TextView, val min: TextView) {

    private var disposable: Disposable? = null

    fun start() {
        trigger()
    }

    fun dispose() {
        disposable?.dispose()
        disposable = null
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
        val priceInfo = getPriceInfoOf(jsonString)
        return priceInfo.current
    }

    private fun getPriceInfoOf(jsonString: String?): PriceInfo {
        try {
            val jsonObject = JSONObject(jsonString)
            val resultCode = jsonObject.get("resultCode")
            if (resultCode == "success") {
                val values = jsonObject.getJSONObject("result").getJSONArray("areas")
                for (i in 0 until values.length()) {
                    val data = values.getJSONObject(i).getJSONArray("datas")
                    for (j in 0 until data.length()) {
                        val cd = data.getJSONObject(i).getString("cd")
                        if (cd == code) {
                            val current = Integer.parseInt(data.getJSONObject(i).getString("nv"))
                            return PriceInfo("", "", current, 0, 0, 0, 0)
                        }
                    }
                }
            }
        } catch (e: ParseException) {
            Log.e(TAG, ": $e")
        }
        return PriceInfo("", "", 0, 0, 0, 0, 0)
    }

    companion object {
        private const val TAG = "Log"
        private const val BASE_URL = "http://polling.finance.naver.com/api/realtime.nhn?query=SERVICE_ITEM:"
    }
}
