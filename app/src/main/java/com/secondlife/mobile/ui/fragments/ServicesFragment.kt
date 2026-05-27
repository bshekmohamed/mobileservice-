package com.secondlife.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.secondlife.mobile.data.model.Service
import com.secondlife.mobile.databinding.FragmentServicesBinding
import com.secondlife.mobile.ui.adapters.ServiceAdapter

class ServicesFragment : Fragment() {

    private var _binding: FragmentServicesBinding? = null
    private val binding get() = _binding!!
    private lateinit var serviceAdapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadServices()
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceAdapter()
        binding.servicesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = serviceAdapter
        }
    }

    private fun loadServices() {
        val services = listOf(
            Service(1, "Screen Repair", "Fix broken screens", 999.0),
            Service(2, "Battery Replacement", "Replace phone battery", 1499.0),
            Service(3, "Charging Port", "Fix charging issues", 799.0),
            Service(4, "Speaker Repair", "Fix audio problems", 599.0),
            Service(5, "Water Damage", "Water damage recovery", 2499.0),
            Service(6, "Software Fix", "Operating system repair", 499.0)
        )
        serviceAdapter.submitList(services)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}