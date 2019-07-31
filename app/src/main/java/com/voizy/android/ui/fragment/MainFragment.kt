package com.voizy.android.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.model.Voizy
import com.voizy.android.ui.adapter.VoizyRecyclerViewAdapter
import com.voizy.android.ui.adapter.VoizySwipeCallback
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.MainFragmentViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File

class MainFragment : Fragment() {

    /*
     * Todos:
     * -done- 1. Delete function
     * -done- 2. Share function
     * - Play function for MainFragment
     * - .mp3 compression
     * -done- Decide how to trigger share function (swipe vs. click?)
     * -done- Swipe to return the row original looking
     * - Shadow for rec button
     */
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

        voizyListAdapter = VoizyRecyclerViewAdapter()
        voizyList = view.findViewById<RecyclerView>(R.id.rv_voizy_list).apply {
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = LinearLayoutManager(context!!)
            adapter = voizyListAdapter
        }

        val swipeCallback = object : VoizySwipeCallback(context!!) {

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
                            "Sharing",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(voizyList)

        viewModel.getOwnVoizys()

        Completable
            .fromAction {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_PERMISSIONS
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe()

        viewModel.getVoizyStream()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                Timber.d("voizysStream received $it")
                voizyListAdapter.addAll(it)
                it.forEach {
                    Timber.d("voizysStream received ${it.name} ${it.filePath}")
                }
            }

        viewModel.getSaveVoizyEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (it.name.isNotEmpty()) {
                    voizyListAdapter.addAll(listOf(it))
                    Snackbar.make(view!!, "Voizy saved. Share and let others enjoy!", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_EXTERNAL_PERMISSIONS &&
            permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Timber.d("READ_EXTERNAL_PERMISSION YES")
            viewModel.getReceivedVoizys()
        } else {
            Timber.d("READ_EXTERNAL_PERMISSION NO")
        }
    }

    private fun shareVoizy(voizy: Voizy) {
        val fileUri: Uri? = try {
            FileProvider.getUriForFile(
                context!!,
                "com.voizy.android.fileprovider",
                File(voizy.filePath)
            )
        } catch (e: IllegalArgumentException) {
            Timber.e(
                e, "File Selector",
                "The selected file can't be shared: ${voizy.filePath}"
            )
            null
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "audio/*"
        }
        startActivity(Intent.createChooser(sendIntent, "Share voizy"))
    }
}