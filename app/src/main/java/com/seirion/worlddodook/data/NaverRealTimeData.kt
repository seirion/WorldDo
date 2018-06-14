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

@Suppress("MemberVisibilityCanBePrivate", "unused")
@JsonClass(generateAdapter = true)
data class NaverRealTimeData(
        @Json(name="cd") val code: String,                 // 종목코드
        @Json(name="nm") val name: String,                 // 종목명
        @Json(name="ov") val open: Int,                    // 시가
        @Json(name="hv") val high: Int,                    // 고가
        @Json(name="lv") val low: Int,                     // 저가
        @Json(name="nv") val current: Int,                 // 종가
        @Json(name="aq") val quantity: Long,               // 거래량
        @Json(name="aa") val amount: Long,                 // 거래대금
        @Json(name="sv") val yesterdayClose: Int,          // 전일 종가
        @Json(name="cv") val changeValue: Int,             // 전일대비 가격 차이
        @Json(name="cr") val changeRate: Float,            // 전일대비 가격 증감 %
        @Json(name="ul") val upperLimit: Int,              // 상한가
        @Json(name="ll") val lowerLimit: Int,              // 하한가
        @Json(name="rf") val changeType: Int,              // 전일대비
        @Json(name="ms") val marketState: String           // 시장 상태
) {
    val changeTypeExplanation
        get() = when (changeType) {
            1 -> "상한가"
            2 -> "상승"
            4 -> "하한가"
            5 -> "하락"
            else -> "보합"
        }

    val markStateExplanation
        get() = when (marketState) {
            "PREOPEN" -> "개장전"
            "CLOSE" -> "장마감"
            else -> "장중"
        }

    override fun toString(): String {
        val sign = when {
            current > yesterdayClose -> "+"
            current < yesterdayClose -> "-"
            else -> ""
        }
        return "$name ($code)\n" +
            "가격 : $current ($sign$changeValue, $sign$changeRate%)\n" +
            "거래 : $quantity (대금 : $amount)\n" +
            "고가 : $high\n" +
            "저가 : $low\n"
    }
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
