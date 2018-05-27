package com.seirion.worlddodook.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

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
) {
    fun asPriceInfo() = PriceInfo(code, name, low, high, open, current)
}

data class StockCodeQueryData(
        val code: String,
        val name: String,
        val market: String,
        val path: String,
        val code2: String
)

@Suppress("unused")
class StockCodeQueryDataAdapter {
    @FromJson fun fromJson(list: List<List<String>>): StockCodeQueryData {
        return StockCodeQueryData(
                code=list[0][0],
                name=list[1][0],
                market=list[2][0],
                path=list[3][0],
                code2=list[4][0]
        )
    }
    @ToJson fun toJson(data: StockCodeQueryData): List<List<String>> {
        return listOf(
                listOf(data.code),
                listOf(data.name),
                listOf(data.market),
                listOf(data.path),
                listOf(data.code2)
        )
    }
}

@JsonClass(generateAdapter = true)
data class StockCodeQueryResponse(
        val query: List<String>,
        val items: List<List<StockCodeQueryData>>
) {
    companion object
}
