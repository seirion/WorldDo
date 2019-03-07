package com.trueedu.world.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChangeEvents
import com.trueedu.world.R
import com.trueedu.world.data.Settings


class SettingActivity : AppCompatActivity() {

    private lateinit var codeNum: EditText
    private lateinit var coolTime: EditText

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        codeNum = findViewById(R.id.code_num)
        codeNum.setText(Settings.codeNum.toString())
        codeNum.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                Log.d(TAG, "lose focus")
                codeNum.setText(Settings.codeNum.toString())
            }
        }
        codeNum.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try {
                    var num = s.toString().toInt()
                    num = maxOf(num, Settings.MIN_CODE_NUM)
                    num = minOf(num, Settings.MAX_CODE_NUM)
                    Settings.codeNum = num
                } catch (e: NumberFormatException) {
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        coolTime = findViewById(R.id.cool_time)
        coolTime.setText(Settings.coolTimeSec.toString())
        coolTime.focusChanges().filter { !it }
                .doOnNext { Log.d(TAG, "lose focus") }
                .subscribe { coolTime.setText(Settings.coolTimeSec.toString()) }

        coolTime.textChangeEvents()
                .map { it.text.toString().toLong() }
                .map { maxOf(it, Settings.MIN_COOL_TIME_SEC) }
                .map { minOf(it, Settings.MAX_COOL_TIME_SEC) }
                .distinctUntilChanged()
                .subscribe({ Settings.coolTimeSec = it }, { /* ignore errors */ })
    }

    companion object {
        private const val TAG = "SettingActivity"
        fun start(activity: Context) {
            activity.startActivity(Intent(activity, SettingActivity::class.java))
        }
    }
}

