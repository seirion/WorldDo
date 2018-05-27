package com.seirion.worlddodook

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.seirion.worlddodook.data.Card
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.selector


class MainActivity : AppCompatActivity() {

    private lateinit var hour: TextView
    private lateinit var min: TextView
    private lateinit var price: TextView

    private lateinit var code: String
    private lateinit var card: Card

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        hour = findViewById(R.id.hour)
        min = findViewById(R.id.min)
        price = findViewById(R.id.price)

        code = initialCode()
        card = Card(code, price, hour, min)

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

    private fun initialCode(): String {
        val prefs = getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(PREFS_DEFAULT_KEY_CODE, "043710") // default ㅅㅇㄹㄱ
    }

    private fun open() {
        val v = layoutInflater.inflate(R.layout.input_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(v)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    async(UI) {
                        val name = v.findViewById<EditText>(R.id.input).text.toString()
                        val deferred = bg { queryStockCodes(name) }
                        val queryStockCodes = deferred.await()
                        val stockNames = queryStockCodes.map { it.name }
                        selector("종목선택", stockNames, { dialog, i ->
                            code = queryStockCodes[i].code
                            card.resume(code)
                            getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                                    .putString(PREFS_DEFAULT_KEY_CODE, code)
                                    .apply()
                        })
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    companion object {
        private const val DEFAULT_PREFS = "DEFAULT_PREFS"
        private const val PREFS_DEFAULT_KEY_CODE = "PREFS_DEFAULT_KEY_CODE"
    }
}
