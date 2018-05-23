package com.seirion.worlddodook.data

data class PriceInfo(val code: String,
                     val name: String,
                     val current: Int,
                     val low: Int,
                     val high: Int,
                     val begin: Int,
                     val end: Int)