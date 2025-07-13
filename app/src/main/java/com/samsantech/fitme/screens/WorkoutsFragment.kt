package com.samsantech.fitme.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout
import com.samsantech.fitme.R

class WorkoutsFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var customTabContainer: LinearLayout
    private lateinit var btnStart: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workouts, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        customTabContainer = view.findViewById(R.id.customTabContainer)
        btnStart = view.findViewById(R.id.btnStart)

        setupTabs()

        btnStart.setOnClickListener {
            Toast.makeText(requireContext(), "Workout Started!", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("CUSTOM"))
        tabLayout.addTab(tabLayout.newTab().setText("PLAN"))
        tabLayout.addTab(tabLayout.newTab().setText("WORKOUTS"))

        loadFragment(CustomWorkoutFragment())
        customTabContainer.visibility = View.VISIBLE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "CUSTOM" -> {
                        loadFragment(CustomWorkoutFragment())
                        customTabContainer.visibility = View.VISIBLE
                    }
                    "PLAN" -> {
                        loadFragment(PlanFragment())
                        customTabContainer.visibility = View.GONE
                    }
                    "WORKOUTS" -> {
                        loadFragment(RecommendedWorkoutsFragment())
                        customTabContainer.visibility = View.GONE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.tabContentContainer, fragment)
        transaction.commit()
    }
}