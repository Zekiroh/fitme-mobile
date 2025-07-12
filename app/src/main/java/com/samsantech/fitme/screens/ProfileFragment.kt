package com.samsantech.fitme.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.samsantech.fitme.auth.LoginActivity
import com.samsantech.fitme.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Go to Profile", Toast.LENGTH_SHORT).show()
        }

        binding.btnMembershipDetails.setOnClickListener {
            Toast.makeText(requireContext(), "Go to Membership Details", Toast.LENGTH_SHORT).show()
        }

        binding.btnFeedbacks.setOnClickListener {
            Toast.makeText(requireContext(), "Go to Feedbacks", Toast.LENGTH_SHORT).show()
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Go to Settings", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        val prefs = requireContext().getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().commit()  // ensures data is cleared immediately

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), com.samsantech.fitme.auth.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        requireActivity().finishAffinity()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
