package com.samsantech.fitme.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R

class FeedbackFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feedback, container, false)

        val btnTakeSurvey = view.findViewById<Button>(R.id.btnTakeSurvey)
        btnTakeSurvey.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/KAiaGp6wKHcfv7cA9"))
            startActivity(intent)
        }

        return view
    }
}
