package com.voizy.android.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.adapter.VoizyListAdapter
import com.voizy.android.ui.adapter.VoizyViewHolder
import com.voizy.android.ui.listener.VoizyActionListener
import com.voizy.android.utils.NetworkState
import com.voizy.android.utils.ShareUtils
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.MainFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.main_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainFragment :
    BaseFragment(),
    VoizyActionListener,
    TextWatcher {

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onBackPressed() {
    }

    private val viewModel: MainFragmentViewModel by inject()
    private lateinit var voizyRecyclerView: RecyclerView
    private lateinit var voizyAdapter: VoizyListAdapter
    private val shareRequests = PublishSubject.create<Voizy>()

    companion object {
        val TAG = MainFragment::class.java.simpleName
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(searchText: CharSequence?, start: Int, before: Int, count: Int) {
        voizyRecyclerView.scrollToPosition(0)
        voizyAdapter.submitList(null)
        viewModel.loadVoizys(searchText.toString())
    }

    override fun shareVoizy(voizy: Voizy) {
        shareRequests.onNext(voizy)
        Snackbar.make(
            view!!,
            getString(R.string.voizy_sharing, voizy.name),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun playVoizy(viewHolder: VoizyViewHolder, position: Int, voizy: Voizy) {
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

        et_search.addTextChangedListener(this)

        voizyRecyclerView = view.findViewById(R.id.rv_voizy_list)

        initListAdapter()

        setObservables()

        viewModel.loadVoizys()
    }

    private fun initListAdapter() {

        voizyAdapter = VoizyListAdapter(this)

        voizyRecyclerView.layoutManager = LinearLayoutManager(
            this.context!!,
            LinearLayoutManager.VERTICAL,
            false
        )
        voizyRecyclerView.adapter = voizyAdapter
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

        viewModel.voizys
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizyAdapter.submitList(it) }

        viewModel.initialLoading
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                initial_loader.visibility =
                    if (it == NetworkState.LOADING) View.VISIBLE else View.GONE
            }

        viewModel.networkState
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizyAdapter.networkState = it }
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