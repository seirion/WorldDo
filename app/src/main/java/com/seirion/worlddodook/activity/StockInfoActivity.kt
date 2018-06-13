package com.seirion.worlddodook.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.seirion.worlddodook.R


class StockInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_info)

        findViewById<View>(R.id.outside).setOnClickListener({ finish() })
        findViewById<TextView>(R.id.stock_info).text = stockInfo
    }

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

