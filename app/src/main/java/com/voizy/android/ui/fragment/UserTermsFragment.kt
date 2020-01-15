package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.voizy.android.R
import com.voizy.android.utils.PreferencesStore
import kotlinx.android.synthetic.main.user_terms_fragment.*
import org.koin.android.ext.android.inject

class UserTermsFragment : BaseFragment() {

    private val prefsStore: PreferencesStore by inject()

    companion object {
        val TAG = UserTermsFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_terms_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        webView.loadUrl("https://www.voizyapp.com/terms")

        cb_user_terms_agree.setOnCheckedChangeListener { _, isChecked ->
            btn_user_agreement_confirm.isEnabled = isChecked
        }

        btn_user_agreement_confirm.setOnClickListener {
            if (cb_user_terms_agree.isChecked) {
                prefsStore.userTermsAgreed.value = true

                // TODO remove this
                // val createOptionsFragment =
                //     fragmentManager!!.findFragmentById(R.id.record_button_fragment)
                //
                // fragmentManager!!.beginTransaction()
                //     .show(createOptionsFragment!!)
                //     .add(R.id.fragment_container, LibraryFragment(), LibraryFragment.TAG)
                //     .commit()
            } else {
                Snackbar.make(
                    view!!, getString(R.string.agree_on_terms_by_ticking), Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}