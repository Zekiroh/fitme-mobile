package com.samsantech.fitme.screens

import android.os.Bundle
import android.text.Html
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R

class TermsOfUseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_use)

        val textTermsContent = findViewById<TextView>(R.id.textTermsContent)
        textTermsContent.text = Html.fromHtml(
            getString(R.string.terms_of_use_html),
            Html.FROM_HTML_MODE_LEGACY
        )

        val backButtonContainer = findViewById<FrameLayout>(R.id.backButtonContainer)
        backButtonContainer.setOnClickListener {
            finish()
        }
    }
}
