package com.seirion.worlddodook

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
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
import android.view.ViewGroup
import android.view.LayoutInflater
import com.jakewharton.rxbinding.view.RxView
import com.seirion.worlddodook.activity.SettingActivity
import com.seirion.worlddodook.ui.WorldViewPager
import kotlinx.coroutines.experimental.Deferred
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: WorldViewPager
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        viewPager = findViewById(R.id.viewPager)
        adapter = Adapter(this, { this.openInputDialog() })
        viewPager.adapter = adapter
        viewPager.currentItem = 1
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
            }
        })

        DataSource.init(this)
        DataSource.observeChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ adapter.updateUi(it) })
    }

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
                DataSource.set(viewPager.currentItem, code)
            }
        }
    }

    private class Adapter(context: Context, listener: (Any) -> Deferred<DialogInterface>) : PagerAdapter() {
        private val activity = context
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private var hour: TextView? = null
        private var min: TextView? = null
        private var price: TextView? = null
        private val listener = listener

        override fun getCount(): Int {
            return 2
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View
            if (position == 0) {
                view = inflater.inflate(R.layout.item_page_about, container, false)
                val appContext = view.context.applicationContext
                view.findViewById<TextView>(R.id.version).text = versionName(appContext)
                val setting = view.findViewById<View>(R.id.settings)
                RxView.clicks(setting).throttleFirst(2, TimeUnit.SECONDS)
                        .subscribe({ SettingActivity.start(activity) })
            } else {
                view = inflater.inflate(R.layout.item_page_card, container, false)
                view.findViewById<View>(R.id.root).setOnLongClickListener {
                    run(listener)
                    return@setOnLongClickListener true
                }

                hour = view.findViewById(R.id.hour)
                min = view.findViewById(R.id.min)
                price = view.findViewById(R.id.price)
            }
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        fun updateUi(prices: List<PriceInfo>) {
            val date = GregorianCalendar()
            hour?.text = String.format(Locale.US, "%02d", date.get(Calendar.HOUR))
            min?.text = String.format(Locale.US, "%02d", date.get(Calendar.MINUTE))
            if (!prices.isEmpty()) {
                // TODO: Display all prices
                price?.text = prices[0].current.toString()
            }
        }
    }
}

fun versionName(applicationContext: Context) =
        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName

fun versionCode(applicationContext: Context) =
        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionCode

