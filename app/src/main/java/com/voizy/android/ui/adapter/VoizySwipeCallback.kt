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
import kotlin.math.absoluteValue

class VoizySwipeCallback(
    private val context: Context,
    private val voizySwipeListener: VoizySwipeListener
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    private val clearPaint = Paint()
    private var deleteDrawable: Drawable = context.getDrawable(R.drawable.ic_mic_white_48dp)
    // private var deleteDrawable: Drawable = context.getDrawable(R.drawable.ic_delete_sweep_white)
    private var shareDrawable: Drawable = context.getDrawable(R.drawable.ic_send_orange_dark)
    private var background: ColorDrawable =
        ColorDrawable(context.getColor(android.R.color.holo_orange_light))
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int

    interface VoizySwipeListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
    }

    init {
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        intrinsicWidth = deleteDrawable.intrinsicWidth
        intrinsicHeight = deleteDrawable.intrinsicHeight
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        voizySwipeListener.onSwiped(viewHolder, direction)
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

        // val isCancelled = dX == 0f && !isCurrentlyActive
        // if (isCancelled) {
        //     clearCanvas(
        //         c,
        //         itemView.right + dX,
        //         itemView.top.toFloat(),
        //         itemView.right.toFloat(),
        //         itemView.bottom.toFloat()
        //     )
        //     super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        //     return
        // }

        if (dX.absoluteValue < (itemView.width * 0.3f)) {
            background = ColorDrawable(context.getColor(android.R.color.white))
            shareDrawable = context.getDrawable(R.drawable.ic_send_orange_dark)
            deleteDrawable = context.getDrawable(R.drawable.ic_mic_white_48dp)
            // deleteDrawable = context.getDrawable(R.drawable.ic_delete_sweep_orange_dark)
        } else {
            background = ColorDrawable(context.getColor(R.color.voizy_orange))
            shareDrawable = context.getDrawable(R.drawable.ic_send_white)
            deleteDrawable = context.getDrawable(R.drawable.ic_mic_white_48dp)
            // deleteDrawable = context.getDrawable(R.drawable.ic_delete_sweep_white)
        }

        when {
            dX > 0 -> { // Swiping to right
                val shareIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val shareIconMargin = (itemHeight - intrinsicHeight) / 2
                val shareIconLeft = itemView.left + shareIconMargin
                val shareIconRight = itemView.left + shareIconMargin + intrinsicWidth
                val shareIconBottom = shareIconTop + intrinsicHeight

                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                background.draw(c)
                shareDrawable.setBounds(
                    shareIconLeft,
                    shareIconTop,
                    shareIconRight,
                    shareIconBottom
                )
                shareDrawable.draw(c)
            }
            dX < 0 -> { // Swiping to left
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight

                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)
                deleteDrawable.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
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