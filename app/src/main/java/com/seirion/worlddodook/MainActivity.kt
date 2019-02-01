package com.seirion.worlddodook

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.jakewharton.rxbinding.view.RxView
import com.seirion.worlddodook.activity.SettingActivity
import com.seirion.worlddodook.activity.StockInfoActivity
import com.seirion.worlddodook.data.Settings
import com.seirion.worlddodook.ui.WorldViewPager
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var viewPager: WorldViewPager
    private lateinit var adapter: Adapter

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        Settings.init(applicationContext)

        viewPager = findViewById(R.id.viewPager)
        adapter = Adapter(this, Settings.codeNum) { this.openInputDialog() }
        viewPager.adapter = adapter
        viewPager.currentItem = Settings.currentPage
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                DataSource.stop()
                if (position < Settings.codeNum) {
                    DataSource.start()
                    Settings.currentPage = position
                }
            }
        })

        DataSource.init(this)
        DataSource.observeChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { adapter.updateUi(viewPager.currentItem, it) }

        Settings.observeSettingChanges()
                .subscribe {
                    adapter.codeNum = it
                    adapter.notifyDataSetChanged()
                }
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

    private class Adapter(context: Activity, codeNum: Int, listener: () -> Unit) : PagerAdapter() {
        companion object {
            private const val DOUBLE_CLICK_THRESHOLD_MS = 500L
        }

        private val activity: Activity = context
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val listener = listener
        private val views = ArrayList<View>(Settings.MAX_CODE_NUM)
        private var prev = 0L // for checking double click
        private var start: Float = 0f // for checking finish condition
        var codeNum = codeNum

        override fun getCount(): Int {
            return 1 + codeNum // 1 is for settings
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View
            if (position == codeNum) {
                view = inflater.inflate(R.layout.item_page_about, container, false)
                view.findViewById<TextView>(R.id.version).text = BuildConfig.VERSION_NAME
                val setting = view.findViewById<View>(R.id.settings)
                RxView.clicks(setting).throttleFirst(2, TimeUnit.SECONDS)
                        .subscribe { SettingActivity.start(activity) }
            } else {
                view = inflater.inflate(R.layout.item_page_card, container, false)
                val root = view.findViewById<View>(R.id.root)
                root.setOnLongClickListener {
                    listener()
                    return@setOnLongClickListener true
                }
                root.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> start = event.y
                        MotionEvent.ACTION_UP -> {
                            if ((event.y - start) < -200f) {
                                activity.finish()
                            }
                        }
                    }
                    false
                }

                RxView.clicks(root).subscribe {
                    val now = SystemClock.elapsedRealtime()
                    if (now - prev <= DOUBLE_CLICK_THRESHOLD_MS) {
                        Log.d(TAG, "double click")
                        showInformation(position)
                    }
                    prev = now
                }
                if (position < views.size) {
                    views[position] = view
                } else {
                    while (views.size <= position) {
                        views.add(view)
                    }
                }
            }
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        fun updateUi(index: Int, prices: List<PriceInfo>) {
            if (index == Settings.codeNum) {
                return
            }

            val date = GregorianCalendar()
            val view = views[index]
            Log.v(TAG, "index($index) : $view")
            view.findViewById<TextView>(R.id.hour).text = String.format(Locale.US, "%02d", date.get(Calendar.HOUR))
            view.findViewById<TextView>(R.id.min).text = String.format(Locale.US, "%02d", date.get(Calendar.MINUTE))

            val targetCode = DataSource.get(index)
            prices.firstOrNull { it.code == targetCode }?.let {
                view.findViewById<TextView>(R.id.price).text = it.current.toString()
            }
        }

        private fun showInformation(position: Int) {
            StockInfoActivity.start(activity, DataSource.get(position))
        }
    }
}
