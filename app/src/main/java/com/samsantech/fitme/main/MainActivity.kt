package com.samsantech.fitme.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.samsantech.fitme.R
import com.samsantech.fitme.screens.*
import com.samsantech.fitme.workouts.WorkoutsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Default fragment
        loadFragment(AssistanceFragment())

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_assistance -> loadFragment(AssistanceFragment())
                R.id.nav_progress -> loadFragment(ProgressFragment())
                R.id.nav_workouts -> loadFragment(WorkoutsFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
