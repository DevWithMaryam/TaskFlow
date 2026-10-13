package com.taskflow.app.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceDecoration(private val spacePx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // Aakhri item ke baad extra gap nahi
        if (parent.getChildAdapterPosition(view) != state.itemCount - 1) {
            outRect.bottom = spacePx
        }
    }
}