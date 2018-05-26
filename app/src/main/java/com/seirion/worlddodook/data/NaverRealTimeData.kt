package com.seirion.worlddodook.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NaverRealTimeResponse(
        val resultCode: String,
        val result: NaverRealTimeResponseResult
) {
    companion object
}

@JsonClass(generateAdapter = true)
data class NaverRealTimeResponseResult(
        val time: String,
        val areas: List<NaverRealTimeResponseArea>,
        val pollingInterval: Int
)

@JsonClass(generateAdapter = true)
data class NaverRealTimeResponseArea(
        val datas: List<NaverRealTimeData>,
        val name: String
)

@JsonClass(generateAdapter = true)
data class NaverRealTimeData(
        @Json(name="cd") val code: String,
        @Json(name="nm") val name: String,
        @Json(name="ov") val open: Int,
        @Json(name="hv") val high: Int,
        @Json(name="lv") val low: Int,
        @Json(name="nv") val current: Int
)

val DUMMY_DATA = NaverRealTimeData("", "",0, 0, 0, 0)

@JsonClass(generateAdapter = true)
data class StockCodeQueryResponse(
        val query: List<String>,
        val items: List<List<List<List<String>>>>
) {
    companion object
}
