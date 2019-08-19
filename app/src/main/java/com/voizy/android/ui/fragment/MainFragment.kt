package com.voizy.android.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.ui.adapter.VoizyRecyclerViewAdapter
import com.voizy.android.ui.adapter.VoizySwipeCallback
import com.voizy.android.ui.listener.OnItemClickListener
import com.voizy.android.ui.model.Voizy
import com.voizy.android.utils.ShareUtils
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.utils.showProgressBar
import com.voizy.android.viewmodels.MainFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainFragment : Fragment(), VoizySwipeCallback.VoizySwipeListener,
    OnItemClickListener<VoizyRecyclerViewAdapter.VoizyViewHolder, Voizy> {

    private val shareRequests = PublishSubject.create<Voizy>()

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val voizy = voizyListAdapter.items[position]
        when (direction) {
            ItemTouchHelper.LEFT -> {

                Snackbar.make(
                    view!!,
                    getString(R.string.voizy_deleted, voizy.name),
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.undo) {
                    voizyListAdapter.cancelDelete()
                    deleteHandler.removeCallbacksAndMessages(null)
                }.show()

                voizyListAdapter.cancellableDelete(position)
                deleteHandler.postDelayed({
                    viewModel.deleteVoizy(voizy)
                }, 3500)
            }
            ItemTouchHelper.RIGHT -> {
                voizyListAdapter.notifyItemChanged(position)
                shareRequests.onNext(voizy)
                Snackbar.make(
                    view!!,
                    getString(R.string.voizy_sharing, voizy.name),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onClick(viewHolder: VoizyRecyclerViewAdapter.VoizyViewHolder, position: Int, voizy: Voizy) {
        Timber.d("onClick $position ${voizy.name} ${voizy.localFilePath}")
        viewModel.playVoizy(voizy)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { viewHolder.animateProgress(it) }
    }

    private val viewModel: MainFragmentViewModel by inject<MainFragmentViewModel>()
    private lateinit var voizyList: RecyclerView
    private lateinit var voizyListAdapter: VoizyRecyclerViewAdapter
    private val deleteHandler = Handler()

    companion object {
        private const val REQUEST_READ_EXTERNAL_PERMISSIONS = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate()")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated()")

        voizyListAdapter = VoizyRecyclerViewAdapter(this)
        voizyList = view.findViewById<RecyclerView>(R.id.rv_voizy_list).apply {
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = LinearLayoutManager(context!!)
            adapter = voizyListAdapter
        }
        ItemTouchHelper(VoizySwipeCallback(context!!, this)).attachToRecyclerView(voizyList)

        setObservables()

        viewModel.fetchVoizys()
    }

    private fun setObservables() {
        shareRequests
            .switchMap { viewModel.downloadVoizy(context!!, it) }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { ShareUtils.shareVoizy(context!!, it) }

        viewModel.getSaveVoizyEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(saveEventObserver())

        viewModel.getVoizyStream()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizyListAdapter.addAll(it) }
    }

    private fun saveEventObserver(): Consumer<Pair<Boolean, Voizy?>> {
        return Consumer { pair ->
            activity!!.showProgressBar(false)
            if (pair.first) {
                Snackbar.make(
                    view!!, getString(R.string.voizy_created_share), Snackbar.LENGTH_LONG
                ).setAction(R.string.share) {
                    shareRequests.onNext(pair.second!!)
                }.show()
            } else {
                Snackbar.make(view!!, getString(R.string.voizy_save_failed), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}