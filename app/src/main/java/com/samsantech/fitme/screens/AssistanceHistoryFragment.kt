package com.samsantech.fitme.screens

import android.content.Context
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.AssistanceMessageComponent
import com.samsantech.fitme.model.AssistanceItem
import com.samsantech.fitme.model.AssistanceReplyRequest
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AssistanceHistoryFragment : Fragment() {
    private lateinit var historyContainer: LinearLayout
    private lateinit var globalTypingIndicator: TextView
    private var userId: Int = -1
    private var activeMeta: TextView? = null
    private var isTyping: Boolean = false
    private var lastHistorySnapshot: String = ""
    private lateinit var socket: Socket
    private var dotCount = 0

    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadHistory()
            handler.postDelayed(this, 3000)
        }
    }

    private val typingDotsRunnable = object : Runnable {
        override fun run() {
            dotCount = (dotCount + 1) % 4
            globalTypingIndicator.text = "Coach is typing" + ".".repeat(dotCount + 1)
            if (isTyping) handler.postDelayed(this, 500)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistance_history, container, false)
        historyContainer = view.findViewById(R.id.historyContainer)
        globalTypingIndicator = view.findViewById(R.id.globalTypingIndicator)

        val sharedPref = requireContext().getSharedPreferences("FitMePrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return view
        }

        initSocket()
        loadHistory()
        handler.postDelayed(refreshRunnable, 8000)

        return view
    }

    private fun initSocket() {
        try {
            socket = IO.socket("http://10.0.2.2:5000")
            socket.connect()

            Log.d("SocketTyping", "Listening on typing-status-$userId")

            socket.on("typing-status-$userId") { args ->
                val data = args[0] as JSONObject
                val isTypingNow = data.getBoolean("isTyping")

                Log.d("SocketTyping", "isTypingNow: $isTypingNow")

                activity?.runOnUiThread {
                    if (isTypingNow) {
                        isTyping = true
                        globalTypingIndicator.visibility = View.VISIBLE
                        dotCount = 0
                        handler.post(typingDotsRunnable)
                    } else {
                        isTyping = false
                        handler.removeCallbacks(typingDotsRunnable)
                        globalTypingIndicator.visibility = View.GONE
                        loadHistory()
                    }
                }
            }

            socket.on("new_reply") {
                activity?.runOnUiThread {
                    loadHistory()
                }
            }

        } catch (e: Exception) {
            Log.e("SocketError", "Failed to connect socket: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(refreshRunnable)
        handler.removeCallbacks(typingDotsRunnable)
        socket.disconnect()
        socket.off("typing-status-$userId")
        socket.off("new_reply")
    }

    private fun loadHistory() {
        RetrofitClient.assistance.getUserAssistance(userId)
            .enqueue(object : Callback<List<AssistanceItem>> {
                override fun onResponse(
                    call: Call<List<AssistanceItem>>,
                    response: Response<List<AssistanceItem>>
                ) {
                    if (response.isSuccessful) {
                        val historyList = response.body() ?: emptyList()
                        val currentSnapshot = historyList.joinToString {
                            "${it.id}-${it.message}-${it.replies?.size ?: 0}"
                        }
                        if (currentSnapshot != lastHistorySnapshot) {
                            lastHistorySnapshot = currentSnapshot
                            populateHistory(historyList)
                        }
                    }
                }

                override fun onFailure(call: Call<List<AssistanceItem>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun formatTimestamp(raw: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("Asia/Manila")
            val parsed = inputFormat.parse(raw)
            val outputFormat = SimpleDateFormat("MMMM d, yyyy - hh:mm a", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Manila")
            outputFormat.format(parsed!!)
        } catch (e: Exception) {
            raw
        }
    }

    private fun populateHistory(history: List<AssistanceItem>) {
        historyContainer.removeAllViews()

        val sortedHistory = history.sortedByDescending {
            try {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.created_at)
            } catch (e: Exception) {
                null
            }
        }

        for (item in sortedHistory) {
            val card = layoutInflater.inflate(R.layout.item_assistance_history, historyContainer, false)
            val txtCategory = card.findViewById<TextView>(R.id.txtCategory)
            val txtStatus = card.findViewById<TextView>(R.id.txtStatus)
            val txtReplies = card.findViewById<LinearLayout>(R.id.txtReplies)
            val btnReply = card.findViewById<Button>(R.id.btnReply)
            val btnSend = card.findViewById<Button>(R.id.btnSendReply)
            val replyBox = card.findViewById<EditText>(R.id.editReply)

            txtCategory.text = "Category: ${item.category}"
            txtStatus.text = item.status
            txtStatus.background = ContextCompat.getDrawable(
                requireContext(),
                if (item.status == "Resolved") R.drawable.status_resolved_background else R.drawable.status_pending_background
            )
            txtStatus.setTextColor(Color.parseColor(if (item.status == "Resolved") "#065F46" else "#92400E"))

            val formattedCreatedAt = formatTimestamp(item.created_at)
            txtReplies.addView(
                AssistanceMessageComponent.create(
                    requireContext(), item.message, "Member", formattedCreatedAt, true
                ) { meta -> toggleMeta(meta) }
            )

            item.replies?.forEach { reply ->
                val formattedReplyTime = formatTimestamp(reply.sent_at)
                txtReplies.addView(
                    AssistanceMessageComponent.create(
                        requireContext(), reply.message,
                        reply.sender.replaceFirstChar { it.uppercaseChar() },
                        formattedReplyTime, reply.sender == "member"
                    ) { meta -> toggleMeta(meta) }
                )
            }

            // ✅ NEW: automatically hide reply button if resolved
            if (item.status == "Resolved") {
                btnReply.visibility = View.GONE
            }

            btnReply.setOnClickListener {
                replyBox.visibility = View.VISIBLE
                btnSend.visibility = View.VISIBLE
                btnReply.visibility = View.GONE
                isTyping = true
            }

            btnSend.setOnClickListener {
                val replyText = replyBox.text.toString().trim()
                if (replyText.isEmpty()) {
                    Toast.makeText(requireContext(), "Reply cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val request = AssistanceReplyRequest(item.id, replyText, "member")
                RetrofitClient.assistance.sendReply(request)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Reply sent!", Toast.LENGTH_SHORT).show()
                                replyBox.text.clear()
                                replyBox.visibility = View.GONE
                                btnSend.visibility = View.GONE
                                btnReply.visibility = View.VISIBLE
                                isTyping = false

                                val nowUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
                                val manilaFormat = SimpleDateFormat("MMMM d, yyyy - hh:mm a", Locale.getDefault())
                                manilaFormat.timeZone = TimeZone.getTimeZone("Asia/Manila")
                                val timestamp = manilaFormat.format(nowUtc)

                                txtReplies.addView(
                                    AssistanceMessageComponent.create(
                                        requireContext(), replyText, "Member", timestamp, true
                                    ) { meta -> toggleMeta(meta) }
                                )
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }

            historyContainer.addView(card)
        }
    }


    private fun toggleMeta(meta: TextView) {
        if (activeMeta != null && activeMeta != meta) {
            activeMeta!!.visibility = View.GONE
        }
        meta.visibility = if (meta.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        activeMeta = if (meta.visibility == View.VISIBLE) meta else null
    }
}
