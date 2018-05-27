package com.seirion.worlddodook

import com.seirion.worlddodook.data.StockCode
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class NaverApiTest {

    @Ignore("attempt to access external api")
    @Test
    fun queryStockCodesShouldReturnMatchedStockCodes() {
        val codes = queryStockCodes("서울리거")
        Assert.assertEquals(listOf(
                StockCode("서울리거", "043710")
        ), codes)
    }

    @Ignore("attempt to access external api")
    @Test
    fun queryStockCodesShouldReturnMultipleItemsListIfThereAreMultipleMatches() {
        val codes = queryStockCodes("한국전력")
        Assert.assertEquals(listOf(
                StockCode("한국전력", "015760"),
                StockCode("한국전력기술", "052690")
        ), codes)
    }

    @Ignore("attempt to access external api")
    @Test
    fun queryStockCodesShouldReturnEmptyListForUnknownStockName() {
        val codes = queryStockCodes("ㅇㅇ없어")
        Assert.assertEquals(listOf<StockCode>(), codes)
    }

    @Test
    fun getPriceInfoShouldReturnMultipleItemList() {
        val priceInfoList = getPriceInfo(listOf("015760", "005930"))
        Assert.assertEquals(listOf("015760", "005930"), priceInfoList.map { it.code })
        Assert.assertEquals(listOf("한국전력", "삼성전자"), priceInfoList.map { it.name })
    }
}
