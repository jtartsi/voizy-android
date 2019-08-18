package com.voizy.android.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
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
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.utils.showProgressBar
import com.voizy.android.viewmodels.MainFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File

class MainFragment : Fragment(), VoizySwipeCallback.VoizySwipeListener,
    OnItemClickListener<VoizyRecyclerViewAdapter.VoizyViewHolder, Voizy> {

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
                shareVoizy(voizy)

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
        viewModel.playVoizy(voizy.localFilePath!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe({
                viewHolder.animateProgress(it)
            }, {
                Toast.makeText(context!!, "Audio file not found", Toast.LENGTH_SHORT).show()
            })
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

        viewModel.getVoizyStream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                voizyListAdapter.addAll(it)
            }

        viewModel.getSaveVoizyEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { pair ->
                activity!!.showProgressBar(false)
                if (pair.first) {
                    voizyListAdapter.addAll(listOf(pair.second!!))
                    Snackbar.make(
                        view!!, getString(R.string.voizy_created_share), Snackbar.LENGTH_LONG
                    ).setAction(R.string.share) {
                        shareVoizy(pair.second!!)
                    }.show()
                } else {
                    Snackbar.make(view!!, getString(R.string.voizy_save_failed), Snackbar.LENGTH_SHORT).show()
                }
            }

        viewModel.fetchVoizys()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
    }

    private fun shareVoizy(voizy: Voizy) {
        val fileUri: Uri? = try {
            FileProvider.getUriForFile(
                context!!,
                "com.voizy.android.fileprovider",
                File(voizy.localFilePath)
            )
        } catch (e: IllegalArgumentException) {
            Timber.e(
                e, "File Selector. The selected file can't be shared: ${voizy.localFilePath}"
            )
            null
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "audio/*"
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_voizy)))
    }
}