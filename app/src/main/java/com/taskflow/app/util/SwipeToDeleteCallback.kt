package com.taskflow.app.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.taskflow.app.R
import kotlin.math.abs

class SwipeToDeleteCallback(
    context: Context,
    private val onSwipedPosition: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val density = context.resources.displayMetrics.density
    private val cornerRadius = 14 * density
    private val iconSize = (24 * density).toInt()
    private val iconMargin = (24 * density).toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.tf_delete)
    }
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!
        .mutate().apply { setTint(Color.WHITE) }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) onSwipedPosition(position)
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
        val item = viewHolder.itemView
        if (dX != 0f) {
            val left = if (dX > 0) item.left.toFloat() else item.right + dX
            val right = if (dX > 0) item.left + dX else item.right.toFloat()
            c.drawRoundRect(
                RectF(left, item.top.toFloat(), right, item.bottom.toFloat()),
                cornerRadius, cornerRadius, paint
            )

            // Icon tab dikhao jab itni jagah ban jaye
            if (abs(dX) > iconMargin + iconSize) {
                val top = item.top + (item.height - iconSize) / 2
                if (dX > 0) {
                    icon.setBounds(
                        item.left + iconMargin, top,
                        item.left + iconMargin + iconSize, top + iconSize
                    )
                } else {
                    icon.setBounds(
                        item.right - iconMargin - iconSize, top,
                        item.right - iconMargin, top + iconSize
                    )
                }
                icon.draw(c)
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}