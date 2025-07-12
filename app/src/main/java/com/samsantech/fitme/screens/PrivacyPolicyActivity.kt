package com.samsantech.fitme.screens

import android.os.Bundle
import android.text.Html
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        val textPolicyContent = findViewById<TextView>(R.id.textPolicyContent)
        textPolicyContent.text = Html.fromHtml(
            getString(R.string.privacy_policy_html),
            Html.FROM_HTML_MODE_LEGACY
        )

        val backButtonContainer = findViewById<FrameLayout>(R.id.backButtonContainer)
        backButtonContainer.setOnClickListener {
            finish()
        }
    }
}
