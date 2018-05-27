package com.seirion.worlddodook

import android.util.Log
import com.seirion.worlddodook.data.DUMMY_DATA
import com.seirion.worlddodook.data.NaverRealTimeData
import com.seirion.worlddodook.data.NaverRealTimeResponse
import com.seirion.worlddodook.data.PriceInfo
import com.seirion.worlddodook.data.StockCode
import com.seirion.worlddodook.data.StockCodeQueryDataAdapter
import com.seirion.worlddodook.data.StockCodeQueryResponse
import com.seirion.worlddodook.data.jsonAdapter
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

private const val STOCK_CODE_QUERY_URL = "http://ac.finance.naver.com:11002/ac?q=%s&q_enc=utf-8&st=111&frm=stock&r_format=json&r_enc=utf-8&r_unicode=1&t_koreng=1&r_lt=111"
private const val PRICE_INFO_QUERY_URL = "http://polling.finance.naver.com/api/realtime.nhn?query=SERVICE_ITEM:%s"
private const val TAG = "Log"

private val moshi: Moshi = Moshi.Builder().add(StockCodeQueryDataAdapter()).build()
private val realtimeResponseAdapter = NaverRealTimeResponse.jsonAdapter(moshi)
private val stockCodeQueryResponseAdapter = StockCodeQueryResponse.jsonAdapter(moshi)

fun getPriceInfo(code: String): Int {
    val client = OkHttpClient()
    val request = Request.Builder()
            .url(PRICE_INFO_QUERY_URL.format(code))
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
        val response = realtimeResponseAdapter.fromJson(jsonString)
        data = response?.result!!.areas[0].datas[0]
    } catch (e: JsonEncodingException) {
        Log.e(TAG, "Fail to parse json: $e", e)
    }
    return PriceInfo(data.code, data.name, data.low, data.high, data.open, data.current)
}

fun queryStockCodes(name: String): List<StockCode> {
    val encodedName = URLEncoder.encode(name, "UTF-8")
    val client = OkHttpClient()
    val request = Request.Builder()
            .url(STOCK_CODE_QUERY_URL.format(encodedName))
            .build()
    val response = client.newCall(request).execute()
    val jsonString = response.body()?.string() ?: return listOf()
    Log.d(TAG, "log : $jsonString")
    val data = stockCodeQueryResponseAdapter.fromJson(jsonString) ?: return listOf()
    return data.items[0].map {
        StockCode(code=it.code, name=it.name)
    }
}