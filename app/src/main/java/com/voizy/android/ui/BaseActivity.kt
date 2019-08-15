package com.voizy.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.voizy.android.R
import com.voizy.android.ui.fragment.BaseFragment
import io.reactivex.subjects.PublishSubject

abstract class BaseActivity : FragmentActivity() {

    protected lateinit var appBarProgressBar: ProgressBar
    private val backPressEvent = PublishSubject.create<Long>()

    override fun onCreateView(parent: View?, name: String?, context: Context?, attrs: AttributeSet?): View {
        return super.onCreateView(parent, name, context, attrs)

        // backPressEvent
        //     .debounce(100, TimeUnit.MILLISECONDS)
        //     .doOnNext { Snackbar.make(parent!!, "Press back again to delete voizy", Snackbar.LENGTH_SHORT).show() }
        //     .timeInterval(TimeUnit.MILLISECONDS)
        //     .skip(1)
        //     .filter {
        //         Timber.d("back-key filter time ${it.time()}")
        //         if (it.time() < 1000) {
        //             Timber.d("back-key pass")
        //             true
        //         } else {
        //             Timber.d("back-key NOT pass")
        //             false
        //         }
        //     }
        //     .subscribe {
        //         Timber.d("back-key subscribe")
        //         supportFragmentManager!!.popBackStackImmediate()
        //     }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment is BaseFragment && currentFragment.doubleBackPress()) {
            supportFragmentManager.popBackStack(
                currentFragment.getBackstackTag(),
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        } else {
            super.onBackPressed()
        }
    }

    fun showProgressBar(visibility: Boolean) {
        appBarProgressBar.visibility = if (visibility) View.VISIBLE else View.GONE
    }
}