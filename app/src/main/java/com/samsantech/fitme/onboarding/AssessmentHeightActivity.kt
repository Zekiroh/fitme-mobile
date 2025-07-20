package com.samsantech.fitme.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.BMIInfoCardView
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.main.MainActivity
import com.samsantech.fitme.model.RegUpdateParam

import com.samsantech.fitme.model.ResponseSuccess
import com.samsantech.fitme.model.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class AssessmentHeightActivity : AppCompatActivity() {

    private lateinit var heightRecycler: RecyclerView
    private lateinit var unitRecycler: RecyclerView
    private lateinit var bmiCard: BMIInfoCardView

    private var selectedGoal = ""
    private var selectedHeight = 171
    private var selectedUnit = "cm"
    private val units = listOf("cm", "ft • in")
    private var isUpdatingAdapter = false
    private var frequency = ""
    private var selectedGender = ""

    private var selectedWeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment_height)

        heightRecycler = findViewById(R.id.heightRecycler)
        unitRecycler = findViewById(R.id.unitRecycler)
        bmiCard = findViewById(R.id.bmiCard)
        selectedGoal = intent.getStringExtra("goal") ?: ""
        selectedWeight = intent.getIntExtra("weight", 0)
        frequency = intent.getStringExtra("frequency") ?: ""
        selectedGender = intent.getStringExtra("gender") ?: ""

        setupHeightRecycler(selectedUnit, selectedHeight.toString())
        initUnitPicker()
        updateBMICard()

        findViewById<View>(R.id.btnNextHeight).setOnClickListener {

            val user = SharedPrefHelper.getLoggedInUser(this)
            user?.let { user ->
            val userId = user.id

            if (userId == 0) {
                Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

                try {
                    RetrofitClient.auth.updateFitReg(
                        userId = userId,
                        body = RegUpdateParam(
                            fitnessPlan = selectedGoal,
                            height = selectedHeight.toString(),
                            gender = selectedGender,
                            weight = selectedWeight,
                            frequency = frequency
                        )
                    ).enqueue(object : Callback<ResponseSuccess> {
                        override fun onResponse(call: Call<ResponseSuccess>, response: Response<ResponseSuccess>) {
                            if (response.isSuccessful && response.body() != null) {

                                RetrofitClient.users.getUsers(userId).enqueue(object: Callback<User> {
                                    override fun onResponse(
                                        call: Call<User?>,
                                        response: Response<User?>
                                    ) {
                                        val user = response.body()
                                        val sharedPrefUsersInfo = getSharedPreferences("usersInfo", MODE_PRIVATE)
                                        sharedPrefUsersInfo.edit().apply {
                                            val gson = Gson()
                                            val userJson = gson.toJson(user)
                                            putString("user_data", userJson)
                                            apply()
                                        }
                                        Toast.makeText(this@AssessmentHeightActivity, "Updated!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@AssessmentHeightActivity, AssessmentCompleteActivity::class.java)
                                        intent.putExtra("height", "170")
                                        intent.putExtra("unit", "cm")
                                        startActivity(intent)
                                        finish()
                                    }
                                    override fun onFailure(
                                        call: Call<User?>,
                                        t: Throwable
                                    ) {
                                        Toast.makeText(this@AssessmentHeightActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                                    }

                                })
                            } else {
                                Toast.makeText(this@AssessmentHeightActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseSuccess>, t: Throwable) {
                            Toast.makeText(this@AssessmentHeightActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    })

                } catch (e: Exception) {
                    Toast.makeText(this@AssessmentHeightActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
//            Toast.makeText(this, "hello world", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, AssessmentCompleteActivity::class.java)
//            intent.putExtra("height", selectedHeight)
//            intent.putExtra("unit", selectedUnit)
//            startActivity(intent)
        }

        findViewById<View>(R.id.skipText).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun initUnitPicker() {
        setupRecycler(
            recyclerView = unitRecycler,
            items = units,
            initialValue = selectedUnit,
            forceSnapHelper = true
        ) { position ->
            val newUnit = units.getOrNull(position) ?: return@setupRecycler
            if (newUnit != selectedUnit) {
                if (selectedUnit == "cm" && newUnit == "ft • in") {
                    selectedHeight = (selectedHeight / 2.54).toInt().coerceIn(39, 87)
                } else if (selectedUnit == "ft • in" && newUnit == "cm") {
                    selectedHeight = (selectedHeight * 2.54).toInt().coerceIn(100, 220)
                }

                selectedUnit = newUnit
                val displayValue = if (newUnit == "cm") {
                    selectedHeight.toString()
                } else {
                    formatFeetInches(selectedHeight)
                }
                setupHeightRecycler(newUnit, displayValue)

                heightRecycler.post {
                    val layoutManager = heightRecycler.layoutManager as? LinearLayoutManager ?: return@post
                    val index = if (newUnit == "cm") selectedHeight - 100 else selectedHeight - 39
                    layoutManager.scrollToPositionWithOffset(index, 0)
                }

                initUnitPicker()
                updateBMICard()
            }
        }
    }

    private fun setupHeightRecycler(unit: String, initialValue: String) {
        val items = if (unit == "cm") {
            (100..220).map { it.toString() }
        } else {
            (39..87).map { formatFeetInches(it) }
        }

        isUpdatingAdapter = true
        setupRecycler(
            recyclerView = heightRecycler,
            items = items,
            initialValue = initialValue
        ) { pos ->
            if (!isUpdatingAdapter) {
                selectedHeight = if (unit == "cm") {
                    items.getOrNull(pos)?.toIntOrNull() ?: selectedHeight
                } else {
                    39 + pos
                }
                updateBMICard()
            }
        }
        isUpdatingAdapter = false
    }

    private fun updateBMICard() {
        // Assume a default selected weight for preview or pull from user profile/state
        val selectedWeight = 70 // Replace with actual weight input when available
        bmiCard.updateBMI(selectedWeight, selectedHeight, selectedUnit)
    }

    private fun formatFeetInches(totalInches: Int): String {
        val feet = totalInches / 12
        val inches = totalInches % 12
        return "${feet}′ ${inches}″"
    }

    private fun setupRecycler(
        recyclerView: RecyclerView,
        items: List<String>,
        initialValue: String,
        forceSnapHelper: Boolean = false,
        onSnap: (Int) -> Unit
    ) {
        recyclerView.clearOnScrollListeners()
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        val adapter = PickerAdapter(items, recyclerView, layoutManager, onSnap)
        recyclerView.adapter = adapter

        val snapHelper = LinearSnapHelper()
        if (forceSnapHelper || recyclerView.onFlingListener == null) {
            recyclerView.onFlingListener = null
            snapHelper.attachToRecyclerView(recyclerView)
        }

        recyclerView.post {
            val center = items.indexOf(initialValue).takeIf { it >= 0 } ?: 0
            layoutManager.scrollToPositionWithOffset(center, 0)
            adapter.setSelected(center)
            onSnap(center)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (isUpdatingAdapter) return
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapView = snapHelper.findSnapView(layoutManager) ?: return
                    val position = recyclerView.getChildAdapterPosition(snapView)
                    if (position != RecyclerView.NO_POSITION) {
                        adapter.setSelected(position)
                        onSnap(position)
                    }
                }
            }
        })
    }

    inner class PickerAdapter(
        private val items: List<String>,
        private val recyclerView: RecyclerView,
        private val layoutManager: LinearLayoutManager,
        private val onItemSelected: (Int) -> Unit
    ) : RecyclerView.Adapter<PickerAdapter.PickerViewHolder>() {

        private var selectedPos = RecyclerView.NO_POSITION

        inner class PickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(android.R.id.text1)

            init {
                view.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        layoutManager.smoothScrollToPosition(recyclerView, RecyclerView.State(), position)
                        setSelected(position)
                        onItemSelected(position)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return PickerViewHolder(view)
        }

        override fun onBindViewHolder(holder: PickerViewHolder, position: Int) {
            val isSelected = position == selectedPos
            holder.text.text = items[position]
            holder.text.textSize = if (isSelected) 28f else 20f
            holder.text.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            holder.text.setTextColor(
                ContextCompat.getColor(
                    this@AssessmentHeightActivity,
                    if (isSelected) R.color.orange_500 else R.color.gray
                )
            )
            holder.text.textAlignment = View.TEXT_ALIGNMENT_CENTER
            holder.text.setShadowLayer(0f, 0f, 0f, 0)
        }

        override fun getItemCount(): Int = items.size

        fun setSelected(position: Int) {
            if (position != selectedPos) {
                val previous = selectedPos
                selectedPos = position
                if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
                notifyItemChanged(position)
            }
        }
    }
}
