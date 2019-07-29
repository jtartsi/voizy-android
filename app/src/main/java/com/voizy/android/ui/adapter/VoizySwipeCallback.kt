package com.voizy.android.ui.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.voizy.android.R

class VoizySwipeCallback(
    private val context: Context
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    private lateinit var deleteDrawable: Drawable
    private lateinit var shareDrawable: Drawable
    private lateinit var background: ColorDrawable

    init {
        deleteDrawable = context.getDrawable(R.drawable.ic_delete_white_sweep)
        shareDrawable = context.getDrawable(R.drawable.ic_send_white)
        background = ColorDrawable(context.getColor(android.R.color.holo_orange_dark))
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
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
            else -> {
                Snackbar.make(viewHolder.itemView, "else $position", Snackbar.LENGTH_SHORT).show()
            }
        }
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

        val itemView = viewHolder.itemView
        val backgoundCornerOffset = 20

        val iconMargin = (itemView.height - shareDrawable.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - shareDrawable.intrinsicHeight) / 2
        val iconBottom = iconTop + shareDrawable.intrinsicHeight

        when {
            dX > 0 -> { // Swiping to right
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + shareDrawable.intrinsicWidth
                shareDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                background.setBounds(
                    itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgoundCornerOffset,
                    itemView.bottom
                )
            }
            dX < 0 -> { // Swiping to left
                val iconLeft = itemView.right - iconMargin - deleteDrawable.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                deleteDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                background.setBounds(
                    itemView.right + dX.toInt() - backgoundCornerOffset,
                    itemView.top, itemView.right, itemView.bottom
                )
            }
            else -> {
                // View is unSwiped
                background.setBounds(0, 0, 0, 0)
            }
        }
        background.draw(c)
        shareDrawable.draw(c)
        // deleteDrawable.draw(c)
    }
}