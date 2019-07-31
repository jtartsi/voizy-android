package com.voizy.android.ui.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.R
import timber.log.Timber
import kotlin.math.absoluteValue

abstract class VoizySwipeCallback(
    private val context: Context
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    private val clearPaint = Paint()
    private val deleteDrawable: Drawable = context.getDrawable(R.drawable.ic_delete_white_sweep)
    private val shareDrawable: Drawable = context.getDrawable(R.drawable.ic_send_white)
    private var background: ColorDrawable = ColorDrawable(context.getColor(android.R.color.holo_orange_light))
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    init {
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        intrinsicWidth = deleteDrawable.intrinsicWidth
        intrinsicHeight = deleteDrawable.intrinsicHeight
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
        val itemHeight = itemView.height

        val isCancelled = dX == 0f && !isCurrentlyActive
        if (isCancelled) {
            clearCanvas(
                c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        if (dX.absoluteValue < (itemView.width * 0.7f)) {
            background = ColorDrawable(context.getColor(android.R.color.holo_orange_light))
        } else {
            background = ColorDrawable(context.getColor(android.R.color.holo_orange_dark))
        }

        when {
            dX > 0 -> { // Swiping to right
                val shareIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val shareIconMargin = (itemHeight - intrinsicHeight) / 2
                val shareIconLeft = itemView.left + shareIconMargin
                val shareIconRight = itemView.left + shareIconMargin + intrinsicWidth
                val shareIconBottom = shareIconTop + intrinsicHeight

                background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                background.draw(c)
                shareDrawable.setBounds(shareIconLeft, shareIconTop, shareIconRight, shareIconBottom)
                shareDrawable.draw(c)
                if (dX > itemView.width) {
                    Timber.d("Trying to clear")
                    clearCanvas(
                        c,
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                }
            }
            dX < 0 -> { // Swiping to left
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight

                background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                background.draw(c)
                deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteDrawable.draw(c)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas, left: Float?, top: Float?, right: Float?, bottom: Float?) {
        c.drawRect(left!!, top!!, right!!, bottom!!, clearPaint)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.7f
    }
}