package com.samsantech.fitme.onboarding

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.samsantech.fitme.R
import com.samsantech.fitme.main.MainActivity

class AssessmentWeightActivity : AppCompatActivity() {

    private lateinit var weightRecycler: RecyclerView
    private lateinit var unitRecycler: RecyclerView

    private var selectedWeight = 55
    private var selectedUnit = "kg"
    private val units = listOf("kg", "lbs")
    private var isUpdatingAdapter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment_weight)

        weightRecycler = findViewById(R.id.weightRecycler)
        unitRecycler = findViewById(R.id.unitRecycler)

        setupWeightRecycler(selectedUnit, selectedWeight.toString())
        initUnitPicker()

        findViewById<View>(R.id.btnNextWeight).setOnClickListener {
            val intent = Intent(this, AssessmentHeightActivity::class.java)
            intent.putExtra("weight", selectedWeight)
            intent.putExtra("unit", selectedUnit)
            startActivity(intent)
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
                // Convert current weight based on selectedUnit BEFORE switching
                selectedWeight = if (newUnit == "kg") {
                    ((selectedWeight / 2.20462).toInt()).coerceIn(30, 150)
                } else {
                    ((selectedWeight * 2.20462).toInt()).coerceIn(66, 330)
                }

                selectedUnit = newUnit
                setupWeightRecycler(newUnit, selectedWeight.toString())

                // ✅ Center the correct value after switching units
                weightRecycler.post {
                    val layoutManager = weightRecycler.layoutManager as? LinearLayoutManager ?: return@post
                    val index = if (newUnit == "kg") {
                        selectedWeight - 30
                    } else {
                        selectedWeight - 66
                    }
                    layoutManager.scrollToPositionWithOffset(index, 0)
                }

                // 🔁 Refresh the unit picker as well
                initUnitPicker()
            }
        }
    }

    private fun setupWeightRecycler(unit: String, initialValue: String) {
        val items = if (unit == "kg") (30..150).map { it.toString() } else (66..330).map { it.toString() }

        isUpdatingAdapter = true
        setupRecycler(
            recyclerView = weightRecycler,
            items = items,
            initialValue = initialValue
        ) { pos ->
            if (!isUpdatingAdapter) {
                selectedWeight = items.getOrNull(pos)?.toIntOrNull() ?: selectedWeight
            }
        }
        isUpdatingAdapter = false
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
                    this@AssessmentWeightActivity,
                    if (isSelected) R.color.orange_500 else R.color.gray
                )
            )
            holder.text.textAlignment = View.TEXT_ALIGNMENT_CENTER
            holder.text.setShadowLayer(0f, 0f, 0f, 0) // ❌ No glow
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
