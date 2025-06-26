package com.samsantech.fitme.info

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R

class TermsOfUseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_use)

        val contentTextView = findViewById<TextView>(R.id.textTermsContent)
        val htmlContent = getString(R.string.terms_of_use_html)
        contentTextView.text = Html.fromHtml(getString(R.string.terms_of_use_html), Html.FROM_HTML_MODE_LEGACY)
        contentTextView.movementMethod = LinkMovementMethod.getInstance() // Enable link clicks

        val backButton = findViewById<ImageView>(R.id.buttonBack)
        backButton.setOnClickListener {
            finish()
        }
    }
}
