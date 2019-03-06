package com.trueedu.world

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
import com.trueedu.world.data.DataSource
import com.trueedu.world.data.PriceInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.editText
import org.jetbrains.anko.selector
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import com.jakewharton.rxbinding3.view.clicks
import com.trueedu.world.activity.SettingActivity
import com.trueedu.world.activity.StockInfoActivity
import com.trueedu.world.data.Settings
import com.trueedu.world.ui.WorldViewPager
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale


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

    private fun openInputDialog() {
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


    @SuppressLint("CheckResult")
    private fun chooseStock(name: String) {
        Observable.fromCallable { queryStockCodes(name) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    val stockNames = list.map { it.name }
                    if (stockNames.isEmpty()) {
                        toast("ㅇㅇ 없어")
                        openInputDialog()
                    } else {
                        selector(null, stockNames) { _, i ->
                            val code = list[i].code
                            DataSource.set(viewPager.currentItem, code)
                        }
                    }
                }, {
                    Log.e(TAG, "Failed to queryStockCodes()")
                    it.printStackTrace()
                })
    }

    private class Adapter(val activity: Activity, var codeNum: Int, val listener: () -> Unit) : PagerAdapter() {
        companion object {
            private const val DOUBLE_CLICK_THRESHOLD_MS = 500L
        }

        private val inflater: LayoutInflater = LayoutInflater.from(activity.applicationContext)
        private val views = ArrayList<View>(Settings.MAX_CODE_NUM)
        private var prev = 0L // for checking double click
        private var start: Float = 0f // for checking finish condition

        override fun getCount(): Int {
            return 1 + codeNum // 1 is for settings
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        @SuppressLint("CheckResult")
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View
            if (position == codeNum) {
                view = inflater.inflate(R.layout.item_page_about, container, false)
                view.findViewById<TextView>(R.id.version).text = BuildConfig.VERSION_NAME
                val setting = view.findViewById<View>(R.id.settings)
                setting.clicks().throttleFirst(2, TimeUnit.SECONDS)
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

                root.clicks().subscribe {
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
