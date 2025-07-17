package com.samsantech.fitme.screens

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.*
import androidx.fragment.app.Fragment
import com.samsantech.fitme.R
import com.samsantech.fitme.databinding.FragmentSettingsBinding
import com.samsantech.fitme.auth.LoginActivity
import java.io.File
import androidx.core.content.edit
import com.google.gson.Gson
import com.samsantech.fitme.api.RetrofitClient
import com.samsantech.fitme.model.ResponseSuccess
import com.samsantech.fitme.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ These are the correct actions:
        binding.btnTermsOfService.setOnClickListener {
            val intent = Intent(requireContext(), TermsOfUseActivity::class.java)
            startActivity(intent)
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            val intent = Intent(requireContext(), PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
//        val prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
//        prefs.edit().clear().apply()
        val sharedPref = context?.getSharedPreferences("usersInfo", Context.MODE_PRIVATE)
        val userJson = sharedPref?.getString("user_data", null)
        val user = Gson().fromJson(userJson, User::class.java)
        RetrofitClient.members.deleteMembersAccount(
            user.id
        ).enqueue(object: Callback<ResponseSuccess>{
            override fun onResponse(
                call: Call<ResponseSuccess?>,
                response: Response<ResponseSuccess?>
            ) {
                if (response.isSuccessful) {
                    // 1. Clear all SharedPreferences
                    val sharedPrefsDir = File(requireContext().applicationInfo.dataDir, "shared_prefs")
                    if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
                        sharedPrefsDir.listFiles()?.forEach { file ->
                            val prefName = file.name.removeSuffix(".xml")
                            val prefs = requireContext().getSharedPreferences(prefName, Context.MODE_PRIVATE)
                            prefs.edit { clear() }
                        }
                    }

                    // 2. Show toast
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()

                    // 3. Redirect to LoginActivity and clear back stack
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)

                    // 4. Optional: Finish the current activity completely
                    requireActivity().finishAffinity()
                }

            }

            override fun onFailure(call: Call<ResponseSuccess?>, t: Throwable) {
                makeText(context, "network errror: ${t.message}", LENGTH_LONG).show()
            }
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
