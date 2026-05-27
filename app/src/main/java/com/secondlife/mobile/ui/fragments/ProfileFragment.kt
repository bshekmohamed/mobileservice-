package com.secondlife.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.secondlife.mobile.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupProfile()
    }

    private fun setupProfile() {
        binding.tvUserName.text = "John Doe"
        binding.tvUserEmail.text = "john@example.com"
        binding.tvUserPhone.text = "+91 9876543210"
        binding.tvUserLocation.text = "New York, USA"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}