package com.seirion.worlddodook.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.seirion.worlddodook.R


class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    companion object {
        fun start(activity: Context) {
            activity.startActivity(Intent(activity, SettingActivity::class.java))
        }
    }
}

