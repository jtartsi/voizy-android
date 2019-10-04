package com.voizy.android.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.ui.adapter.VoizyRecyclerViewAdapter
import com.voizy.android.ui.listener.VoizyActionListener
import com.voizy.android.ui.models.Voizy
import com.voizy.android.utils.ShareUtils
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.MainFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainFragment :
    BaseFragment(),
    VoizyActionListener,
    TextWatcher {

    override fun onBackPressed() {
    }

    private val viewModel: MainFragmentViewModel by inject()
    private lateinit var voizyList: RecyclerView
    private lateinit var voizyListAdapter: VoizyRecyclerViewAdapter
    private val shareRequests = PublishSubject.create<Voizy>()

    companion object {
        public val TAG = MainFragment::class.java.simpleName
    }

    override fun getBackstackTag(): String {
        return TAG
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(searchText: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.searchVoizys(searchText.toString())
    }

    override fun shareVoizy(voizy: Voizy) {
        shareRequests.onNext(voizy)
        Snackbar.make(
            view!!,
            getString(R.string.voizy_sharing, voizy.name),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun playVoizy(
        viewHolder: VoizyRecyclerViewAdapter.VoizyViewHolder,
        position: Int,
        voizy: Voizy
    ) {
        viewModel.playVoizy(voizy)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { viewHolder.animateProgress(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate()")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated()")

        val editTextSearch = view.findViewById<EditText>(R.id.et_search)
        editTextSearch.addTextChangedListener(this)

        voizyListAdapter = VoizyRecyclerViewAdapter(this)
        voizyList = view.findViewById<RecyclerView>(R.id.rv_voizy_list).apply {
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = LinearLayoutManager(context!!)
            adapter = voizyListAdapter
        }

        setObservables()

        viewModel.searchVoizys()
    }

    private fun setObservables() {
        shareRequests
            .switchMap { viewModel.downloadVoizy(context!!, it) }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { ShareUtils.shareVoizy(it.first, context!!, it.second) }

        viewModel.getSaveVoizyEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(saveEventObserver())

        viewModel.getVoizyStream()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                voizyListAdapter.clear()
                voizyListAdapter.addAll(it)
            }
    }

    private fun saveEventObserver(): Consumer<Pair<Boolean, Voizy?>> {
        return Consumer { pair ->
            if (pair.first) {
                Snackbar.make(
                    view!!, getString(R.string.voizy_created_share), Snackbar.LENGTH_LONG
                ).setAction(R.string.share) {
                    shareRequests.onNext(pair.second!!)
                }.show()
            } else {
                Snackbar.make(view!!, getString(R.string.voizy_save_failed), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }
}