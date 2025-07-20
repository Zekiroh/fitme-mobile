package com.samsantech.fitme.payments

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.samsantech.fitme.R

class FailedActivity: AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_failed_payment)

        findViewById<Button>(R.id.paymentButtonFailed).setOnClickListener {
            finish()
        }
    }
}