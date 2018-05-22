package com.seirion.worlddodook

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var hour: TextView
    private lateinit var min: TextView
    private lateinit var price: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hour = findViewById(R.id.hour)
        min = findViewById(R.id.min)
        price = findViewById(R.id.price)
    }

    private var disposable: Disposable? = null

    override fun onStart() {
        disposable = Observable.interval(30, TimeUnit.SECONDS).startWith(0)
                .flatMap {
                    Observable.just(getPriceInfo())
                }
                .subscribeOn(Schedulers.io())
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val date = GregorianCalendar()
                    hour.text = date.get(Calendar.HOUR).toString()
                    min.text = date.get(Calendar.MINUTE).toString()
                    price.text = it.toString()
                }, { Log.e(TAG, "error : $it") })
        super.onStart()
    }

    private fun getPriceInfo(): Int {
        return 0
    }

    override fun onStop() {
        disposable?.dispose()
        disposable = null
        super.onStop()
    }

    companion object {
        private const val TAG = "Log"
    }
}
