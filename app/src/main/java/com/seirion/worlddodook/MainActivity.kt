package com.seirion.worlddodook

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.seirion.worlddodook.data.DataSource
import com.seirion.worlddodook.data.PriceInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        hour = findViewById(R.id.hour)
        min = findViewById(R.id.min)
        price = findViewById(R.id.price)

        findViewById<View>(R.id.root).setOnLongClickListener {
            openInputDialog()
            return@setOnLongClickListener true
        }

        DataSource.init(this)
        DataSource.observeChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({updateUi(it)})
    }

    private fun versionName() =
        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName

    private fun versionCode() =
        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionCode

    override fun onStart() {
        DataSource.start()
        super.onStart()
    }

    override fun onStop() {
        DataSource.stop()
        super.onStop()
    }

    override fun onDestroy() {
        DataSource.clear()
        super.onDestroy()
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

    private fun updateUi(prices: List<PriceInfo>) {
        val date = GregorianCalendar()
        hour.text = String.format(Locale.US, "%02d", date.get(Calendar.HOUR))
        min.text = String.format(Locale.US, "%02d", date.get(Calendar.MINUTE))
        if (!prices.isEmpty()) {
            // TODO: Display all prices
            price.text = prices[0].current.toString()
        }
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
                val code = queryStockCodes[i].code
                DataSource.set(code)
            }
        }
    }
}
