package com.samsantech.fitme.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.components.SharedPrefHelper
import com.samsantech.fitme.model.MembershipPlan
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembershipDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_membership_detailss, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchMembershipDetails()
    }

    private fun fetchMembershipDetails() {
        val user = SharedPrefHelper.getLoggedInUser(context)
        user?.let {
            RetrofitClient.members.getMemberShip(it.id)
                .enqueue(object : Callback<MembershipPlan> {
                    override fun onResponse(
                        call: Call<MembershipPlan>,
                        response: Response<MembershipPlan>
                    ) {
                        if (response.isSuccessful) {
                            val subscription = response.body()
                            val valuePlan =  view?.findViewById<TextView>(R.id.value_plan)
                            val memberShip = view?.findViewById<TextView>(R.id.value_membership_ends)
                            valuePlan?.text = subscription?.plan
                            memberShip?.text = subscription?.startDate ?: "N/A"
                            // TODO: Use the data (e.g., update UI)
                        } else {
                            Log.e("MembershipDetails", "Error: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<MembershipPlan>, t: Throwable) {
                        Log.e("MembershipDetails", "Failed: ${t.message}")
                    }
                })
        }

    }
}
