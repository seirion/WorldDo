package com.seirion.worlddodook

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.seirion.worlddodook.data.Card
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.customView
import org.jetbrains.anko.editText
import org.jetbrains.anko.selector
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton


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
            openInputDialog()
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

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun openInputDialog() = async(UI) {
        alert {
            lateinit var stockNameEditText: EditText
            customView {
                stockNameEditText = editText {
                    singleLine = true
                    hint = "종목이름"
                }
            }
            yesButton {
                chooseStock(stockNameEditText.text.toString())
            }
        }.show()
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun chooseStock(name: String) = async(UI) {
        val deferred = bg { queryStockCodes(name) }
        val queryStockCodes = deferred.await()
        val stockNames = queryStockCodes.map { it.name }
        if (stockNames.isEmpty()) {
            toast("ㅇㅇ 없어")
            openInputDialog()
        } else {
            selector(null, stockNames) { _, i ->
                code = queryStockCodes[i].code
                card.resume(code)
                getSharedPreferences(DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                        .putString(PREFS_DEFAULT_KEY_CODE, code)
                        .apply()
            }
        }
    }

    companion object {
        private const val DEFAULT_PREFS = "DEFAULT_PREFS"
        private const val PREFS_DEFAULT_KEY_CODE = "PREFS_DEFAULT_KEY_CODE"
    }
}
