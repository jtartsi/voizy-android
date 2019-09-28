package com.voizy.android.ui

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.android.lifecycle.autoDisposable
import com.voizy.android.R
import com.voizy.android.ui.fragment.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class BaseActivity : FragmentActivity() {

    private val backPressEvent = PublishSubject.create<String>()
    private lateinit var rootView: View

    companion object {
        private const val ACCEPT_BACK_THRESHOLD = 3000
    }

    override fun onStart() {
        super.onStart()
        rootView = findViewById(android.R.id.content)

        backPressEvent
            .debounce(100, TimeUnit.MILLISECONDS)
            .timeInterval(TimeUnit.MILLISECONDS)
            .filter {
                if (it.time() < ACCEPT_BACK_THRESHOLD) {
                    true
                } else {
                    Snackbar.make(
                        rootView,
                        resources.getText(R.string.press_back_again_discard_voizy),
                        Snackbar.LENGTH_LONG
                    ).show()
                    false
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe {
                supportFragmentManager!!.popBackStackImmediate(
                    it.value(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is BaseFragment && currentFragment.doubleBackPress()) {
            backPressEvent.onNext(currentFragment.getBackstackTag())
        } else {
            super.onBackPressed()
        }
    }
}