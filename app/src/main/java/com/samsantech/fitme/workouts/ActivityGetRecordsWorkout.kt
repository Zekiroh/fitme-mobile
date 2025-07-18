package com.samsantech.fitme.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.GetWorkoutItem
import com.samsantech.fitme.model.GetWorkoutResponses
import com.samsantech.fitme.model.WorkoutGroup
import com.samsantech.fitme.model.WorkoutItem
import com.samsantech.fitme.model.WorkoutResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityGetRecordsWorkout : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_record_workouts)

        recyclerView = findViewById(R.id.expandableRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val backButton: ImageButton = findViewById(R.id.backButtonRecord)
        backButton.setOnClickListener {
            finish()
        }
        val user = SharedPrefHelper.getLoggedInUser(this)
        user?.let { data ->
            RetrofitClient.members.getWorkouts(data.id).enqueue(
                object: Callback<GetWorkoutResponses> {
                    override fun onResponse(
                        call: Call<GetWorkoutResponses?>,
                        response: Response<GetWorkoutResponses?>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val workoutItems = response.body()!!.workouts

                            val groupedWorkouts = parseAndGroupWorkouts(workoutItems)
                            adapter = WorkoutAdapter(groupedWorkouts)
                            recyclerView.adapter = adapter
                        } else {
                            // Handle API failure (e.g., 404, 500)
                            Toast.makeText(this@ActivityGetRecordsWorkout, "Failed to load workouts", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<GetWorkoutResponses?>,
                        t: Throwable
                    ) {

                    }

                }
            )

        }

    }

    private fun parseAndGroupWorkouts(workouts: List<GetWorkoutItem>): List<WorkoutGroup> {
        return workouts
            .groupBy { it.description }
            .map { (description, items) ->
                WorkoutGroup(
                    description = description,
                    items = items.map {
                        WorkoutItem(
                            id = it.id,
                            weight = it.weight,
                            reps = it.reps,
                            createdAt = it.createdAt
                        )
                    },
                    isExpanded = false // collapsed by default
                )
            }
    }

}

