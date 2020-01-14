package com.voizy.android.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.PlaybackEvent
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.WebViewActivity
import com.voizy.android.ui.adapter.VoizyListAdapter
import com.voizy.android.ui.adapter.VoizyViewHolder
import com.voizy.android.utils.NetworkState
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.utils.toPair
import com.voizy.android.viewmodels.LibraryFragmentViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.library_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LibraryFragment : BaseFragment() {

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onBackPressed() {
    }

    private val viewModel: LibraryFragmentViewModel by inject()
    private lateinit var voizyRecyclerView: RecyclerView
    private lateinit var voizyListAdapter: VoizyListAdapter
    private val shareRequests = PublishSubject.create<Voizy>()
    private val clipBoardRequests = PublishSubject.create<Voizy>()
    private lateinit var searchTextChanges: Observable<CharSequence>

    companion object {
        val TAG = LibraryFragment::class.java.simpleName
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
        searchTextChanges = RxTextView.textChanges(et_search)
            .toFlowable(BackpressureStrategy.LATEST)
            .toObservable()
            .share()
    }

    override fun onStart() {
        super.onStart()

        initLoader()
        initVoizyListing()
        initSearch()
        initLibraryHeader()
        initShare()
        initCopyToClipBoard()
        initPrivacyPolicy()
        initPlayback()
        initResultsState()
        initShowCreateOptions()
    }

    override fun onStop() {
        super.onStop()
        viewModel.releasePlayer()
    }

    private fun initPlayback() {
        voizyListAdapter.onPlayEvent = { viewHolder: VoizyViewHolder, i: Int, voizy: Voizy ->
            viewModel.togglePlay(voizy)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(getScopeProvider())
                .subscribe {
                    when (it.playbackEvent) {
                        PlaybackEvent.START -> {
                            voizyListAdapter.showPlayingIndicator(
                                viewHolder,
                                it.audioDurationInMs
                            )
                        }
                        PlaybackEvent.STOP -> {
                            voizyListAdapter.clearPlayingState()
                        }
                        PlaybackEvent.SWITCH -> {
                            voizyListAdapter.clearPlayingState()
                            voizyListAdapter.showPlayingIndicator(
                                viewHolder,
                                it.audioDurationInMs
                            )
                        }
                    }
                }
        }
    }

    private fun initVoizyListing() {
        voizyListAdapter = VoizyListAdapter()
        voizyRecyclerView.layoutManager = LinearLayoutManager(
            this.context!!,
            LinearLayoutManager.VERTICAL,
            false
        )
        voizyRecyclerView.adapter = voizyListAdapter

        viewModel.voizys
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizyListAdapter.submitList(it) }

        viewModel.networkState
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizyListAdapter.networkState = it }

        viewModel.loadVoizys()
    }

    private fun initLoader() {
        viewModel.initialLoading
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                progress_initial_loader.visibility =
                    if (it == NetworkState.LOADING) View.VISIBLE else View.INVISIBLE
            }
    }

    private fun initResultsState() {
        viewModel.initialLoading
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                Timber.d("search-change initResultsState() $it")
                if (it == NetworkState.LOADING) {
                    layout_no_results.visibility = View.GONE
                } else {
                    layout_no_results.visibility =
                        if (voizyListAdapter.itemCount > 0) View.GONE else View.VISIBLE
                }
            }
    }

    private fun initLibraryHeader() {
        Observable.combineLatest(
            searchTextChanges.delay(1, TimeUnit.SECONDS),
            viewModel.initialLoading, toPair()
        )
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                Timber.d("search-change initLibraryHeader().subscribe() ${it.first}, ${it.second}")
                if (it.first.isNullOrEmpty() && it.second != NetworkState.LOADING) {
                    Timber.d("search-change initLibraryHeader().subscribe() visible")
                    tv_library_headline.visibility = View.VISIBLE
                } else {
                    Timber.d("search-change initLibraryHeader().subscribe() gone")
                    tv_library_headline.visibility = View.GONE
                }
            }
    }

    private fun initSearch() {
        searchTextChanges
            .autoDisposable(getScopeProvider())
            .subscribe { searchText ->
                Timber.d("search-change initSearch().subscribe() $searchText")
                voizyRecyclerView.scrollToPosition(0)
                voizyListAdapter.submitList(null)
                viewModel.loadVoizys(searchText.toString())
            }
        // et_search.addTextChangedListener(object : TextWatcher {
        //     override fun afterTextChanged(s: Editable?) {
        //     }
        //
        //     override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //     }
        //
        //     override fun onTextChanged(
        //         searchText: CharSequence?,
        //         start: Int,
        //         before: Int,
        //         count: Int
        //     ) {
        //         voizyRecyclerView.scrollToPosition(0)
        //         voizyListAdapter.submitList(null)
        //         viewModel.loadVoizys(searchText.toString())
        //
        //     }
        // })
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

        voizyListAdapter.onLongPress = { _: VoizyViewHolder, _: Int, voizy: Voizy ->
            clipBoardRequests.onNext(voizy)
        }
    }

    private fun initShare() {
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

        voizyListAdapter.onShareEvent = { _: VoizyViewHolder, _: Int, voizy: Voizy ->
            shareRequests.onNext(voizy)
        }
    }

    private fun initPrivacyPolicy() {
        btn_privacy_policy.setOnClickListener {
            val intent = Intent(context, WebViewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initShowCreateOptions() {
        fragmentManager!!.addOnBackStackChangedListener {
            val topFragment = fragmentManager!!.findFragmentById(R.id.fragment_container)
            if (topFragment != null &&
                topFragment is BaseFragment &&
                topFragment.tag == TAG
            ) {
                var createOptionsFragment =
                    fragmentManager!!.findFragmentById(R.id.record_button_fragment)!!

                fragmentManager!!.beginTransaction()
                    .show(createOptionsFragment)
                    .commit()
            }
        }
    }
}