package com.trueedu.world.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.trueedu.world.R
import com.trueedu.world.data.DataSource
import com.trueedu.world.data.PriceInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class StockInfoActivity : AppCompatActivity() {

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_info)

        setFinishOnTouchOutside(true)
        findViewById<View>(R.id.outside).setOnClickListener { finish() }

        disposable = DataSource.observeChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updateUi(it) }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(priceInfoList: List<PriceInfo>) {
        findViewById<TextView>(R.id.stock_info).text = priceInfo(priceInfoList, code) +
                formatting(DataSource.updateTime)
    }

    private fun priceInfo(infoList: List<PriceInfo>, code: String) =
            infoList.firstOrNull { it.code == code }?.let {
                Log.d(TAG, "info : $it")
                it.toString()
            } ?: ""

    private fun formatting(date: Date?) =
        date?.let {
            dateFormat.format(it)
        } ?: ""

    companion object {
        private const val TAG = "PrinceInfoActivity"
        private val dateFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        private var code: String = ""

        fun start(activity: Context, code: String) {
            StockInfoActivity.code = code
            val intent = Intent(activity, StockInfoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity.startActivity(intent)
        }
    }
}

