package com.voizy.android.ui.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.voizy.android.R

class VoizySwipeCallback(private val context: Context) : ItemTouchHelper.SimpleCallback(
    0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    private lateinit var deleteDrawable: Drawable
    private lateinit var shareDrawable: Drawable
    private lateinit var background: ColorDrawable

    init {
        shareDrawable = context.getDrawable(R.drawable.ic_send_white)
        background = ColorDrawable(context.getColor(android.R.color.holo_orange_dark))
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> {
                Snackbar.make(viewHolder.itemView, "Deleting $position", Snackbar.LENGTH_SHORT).show()
            }
            ItemTouchHelper.RIGHT -> {
                Snackbar.make(viewHolder.itemView, "Sharing $position", Snackbar.LENGTH_SHORT).show()
            }
        }

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        if (dX > 0) { // Swiping to right
        } else if (dX < 0) { // Swiping to left
        } else { // View is unSwiped
        }
        val itemView = viewHolder.itemView
        val backgoundCornerOffset = 20
    }
}