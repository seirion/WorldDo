package com.seirion.worlddodook

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.seirion.worlddodook.data.Card


class MainActivity : AppCompatActivity() {

    private lateinit var hour: TextView
    private lateinit var min: TextView
    private lateinit var price: TextView

    private var code: String? = null
    private lateinit var card: Card

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        hour = findViewById(R.id.hour)
        min = findViewById(R.id.min)
        price = findViewById(R.id.price)

        code = "043710" // default ㅅㅇㄹㄱ
        card = Card(code!!, price, hour, min)

        findViewById<View>(R.id.root).setOnLongClickListener {
            open()
            return@setOnLongClickListener true
        }
    }

    override fun onStart() {
        super.onStart()
        card.start()
    }

    override fun onStop() {
        card.dispose()
        super.onStop()
    }

    private fun open() {
        val v = layoutInflater.inflate(R.layout.input_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(v)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    code = v.findViewById<EditText>(R.id.input).text.toString()
                    card.resume(code!!)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }
}
