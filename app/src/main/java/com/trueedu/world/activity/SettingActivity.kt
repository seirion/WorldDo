package com.trueedu.world.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChangeEvents
import com.trueedu.world.R
import com.trueedu.world.data.Settings
import com.trueedu.world.rx.ActivityLifecycle
import com.trueedu.world.rx.RxAppCompatActivity


class SettingActivity : RxAppCompatActivity() {

    private lateinit var codeNum: EditText
    private lateinit var coolTime: EditText

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        codeNum = findViewById(R.id.code_num)
        codeNum.setText(Settings.codeNum.toString())
        codeNum.focusChanges()
                .takeUntil(getLifecycleSignal(ActivityLifecycle.DESTROY))
                .filter { !it }
                .doOnNext { Log.d(TAG, "lose focus") }
                .subscribe { codeNum.setText(Settings.codeNum.toString()) }

        codeNum.textChangeEvents()
                .takeUntil(getLifecycleSignal(ActivityLifecycle.DESTROY))
                .filter { !it.text.isNullOrBlank() }
                .map { it.text.toString().toInt() }
                .map { maxOf(it, Settings.MIN_CODE_NUM) }
                .map { minOf(it, Settings.MAX_CODE_NUM) }
                .subscribe({ Settings.codeNum = it }, { /* ignore errors */ })

        coolTime = findViewById(R.id.cool_time)
        coolTime.setText(Settings.coolTimeSec.toString())
        coolTime.focusChanges()
                .takeUntil(getLifecycleSignal(ActivityLifecycle.DESTROY))
                .filter { !it }
                .doOnNext { Log.d(TAG, "lose focus") }
                .subscribe { coolTime.setText(Settings.coolTimeSec.toString()) }

        coolTime.textChangeEvents()
                .takeUntil(getLifecycleSignal(ActivityLifecycle.DESTROY))
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

