package com.seirion.worlddodook.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.seirion.worlddodook.R
import com.seirion.worlddodook.data.DataSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class StockInfoActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_info)

        findViewById<View>(R.id.outside).setOnClickListener({ finish() })
        findViewById<TextView>(R.id.stock_info).text = stockInfo + formatting(DataSource.updateTime)
    }

    private fun formatting(date: Date?) =
        date?.let {
            val dateFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
            dateFormat.format(it)
        } ?: ""

    companion object {
        private var stockInfo: String? = null
        fun start(activity: Context, text: String) {
            stockInfo = text
            val intent = Intent(activity, StockInfoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity.startActivity(intent)
        }
    }
}

