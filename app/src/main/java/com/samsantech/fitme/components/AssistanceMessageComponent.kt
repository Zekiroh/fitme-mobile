package com.samsantech.fitme.components

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.samsantech.fitme.R

object AssistanceMessageComponent {
    fun create(
        context: Context,
        message: String,
        sender: String,
        timestamp: String,
        isMember: Boolean,
        onMetaClick: (TextView) -> Unit
    ): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
                gravity = if (isMember) Gravity.END else Gravity.START
            }
        }

        val bubble = TextView(context).apply {
            text = message
            textSize = 13f
            setPadding(24, 12, 24, 12)
            background = ContextCompat.getDrawable(
                context,
                if (isMember) R.drawable.reply_bubble_orange else R.drawable.reply_bubble_dark
            )
            setTextColor(Color.WHITE)
        }

        val meta = TextView(context).apply {
            text = "$sender • $timestamp"
            textSize = 11f
            setTextColor(Color.GRAY)
            setPadding(4, 4, 4, 0)
            visibility = View.GONE
            gravity = if (isMember) Gravity.END else Gravity.START
        }

        bubble.setOnClickListener { onMetaClick(meta) }

        container.addView(bubble)
        container.addView(meta)
        return container
    }
}