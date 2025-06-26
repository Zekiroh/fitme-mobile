package com.samsantech.fitme.info

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        val contentTextView = findViewById<TextView>(R.id.textPolicyContent)
        val htmlContent = getString(R.string.privacy_policy_html)
        contentTextView.text = Html.fromHtml(getString(R.string.privacy_policy_html), Html.FROM_HTML_MODE_LEGACY)
        contentTextView.movementMethod = LinkMovementMethod.getInstance() // Enable link clicks

        val backButton = findViewById<ImageView>(R.id.buttonBack)
        backButton.setOnClickListener {
            finish()
        }
    }
}
