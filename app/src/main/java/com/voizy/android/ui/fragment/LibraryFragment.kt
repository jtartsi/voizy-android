package com.voizy.android.ui.fragment

import android.content.Intent
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
import com.voizy.android.ui.WebViewActivity
import com.voizy.android.ui.adapter.VoizyListAdapter
import com.voizy.android.ui.adapter.VoizyViewHolder
import com.voizy.android.ui.listener.VoizyActionListener
import com.voizy.android.utils.NetworkState
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.LibraryFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.library_fragment.*
import org.koin.android.ext.android.inject

class LibraryFragment :
    BaseFragment(),
    VoizyActionListener {

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onBackPressed() {
    }

    private val viewModel: LibraryFragmentViewModel by inject()
    private lateinit var voizyRecyclerView: RecyclerView
    private lateinit var voizyAdapter: VoizyListAdapter
    private val shareRequests = PublishSubject.create<Voizy>()
    private val clipBoardRequests = PublishSubject.create<Voizy>()

    companion object {
        val TAG = LibraryFragment::class.java.simpleName
    }

    override fun shareVoizy(voizy: Voizy) {
        shareRequests.onNext(voizy)
    }

    override fun playVoizy(viewHolder: VoizyViewHolder, position: Int, voizy: Voizy) {
        viewModel.togglePlay(voizy)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { viewHolder.animateProgress(it) }
    }

    override fun onVoizyLongPress(voizy: Voizy) {
        clipBoardRequests.onNext(voizy)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.library_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voizyRecyclerView = view.findViewById(R.id.rv_voizy_list)
    }

    override fun onStart() {
        super.onStart()

        initLoader()
        initVoizyListing()
        initSearch()
        initSharing()
        initCopyToClipBoard()
        initSaveNotifications()
        initPrivacyPolicy()
    }

    private fun initLoader() {
        viewModel.initialLoading
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                initial_loader.visibility =
                    if (it == NetworkState.LOADING) View.VISIBLE else View.GONE
            }
    }

    private fun initVoizyListing() {
        voizyAdapter = VoizyListAdapter(this)
        voizyRecyclerView.layoutManager = LinearLayoutManager(
            this.context!!,
            LinearLayoutManager.VERTICAL,
            false
        )
        voizyRecyclerView.adapter = voizyAdapter

        viewModel.voizys
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizyAdapter.submitList(it) }

        viewModel.networkState
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizyAdapter.networkState = it }

        viewModel.loadVoizys()
    }

    private fun initSearch() {
        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(
                searchText: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                voizyRecyclerView.scrollToPosition(0)
                voizyAdapter.submitList(null)
                viewModel.loadVoizys(searchText.toString())
            }
        })
    }

    private fun initCopyToClipBoard() {
        clipBoardRequests.switchMap { viewModel.downloadUrlToClipboard(context!!, it) }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                Snackbar.make(
                    view!!, getString(R.string.url_copied_to_clipboard), Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun initSaveNotifications() {
        viewModel.getSaveVoizyEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(saveEventConsumer())
    }

    private fun initSharing() {
        shareRequests
            .doOnNext {
                Snackbar.make(
                    view!!,
                    getString(R.string.voizy_sharing, it.name),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            .switchMap { viewModel.downloadVoizy(context!!, it) }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.startVoizyShare(context!!, it.first, it.second) }
    }

    private fun initPrivacyPolicy() {
        btn_privacy_policy.setOnClickListener {
            val intent = Intent(context, WebViewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveEventConsumer(): Consumer<Pair<Boolean, Voizy?>> {
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