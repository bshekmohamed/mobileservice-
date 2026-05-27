package com.secondlife.mobile.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.secondlife.mobile.R
import com.secondlife.mobile.databinding.FragmentDashboardBinding
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Branch chip selector
        binding.chipAll.setOnClickListener { viewModel.setBranch("All") }
        binding.chipMimisal.setOnClickListener { viewModel.setBranch("Mimisal") }
        binding.chipRPudu.setOnClickListener { viewModel.setBranch("R.Puduppattinam") }

        viewModel.activeBranch.observe(viewLifecycleOwner) { branch ->
            binding.chipAll.isChecked = branch == "All"
            binding.chipMimisal.isChecked = branch == "Mimisal"
            binding.chipRPudu.isChecked = branch == "R.Puduppattinam"
            binding.tvBranchLabel.text = if (branch == "All") "All Branches" else branch
        }

        viewModel.customerCount.observe(viewLifecycleOwner) {
            binding.tvCustomerCount.text = it.toString()
        }
        viewModel.activeJobCount.observe(viewLifecycleOwner) {
            binding.tvActiveJobs.text = it.toString()
        }
        viewModel.lowStockCount.observe(viewLifecycleOwner) {
            binding.tvLowStock.text = it.toString()
            binding.tvLowStockWarning.visibility = if (it > 0) View.VISIBLE else View.GONE
        }
        viewModel.totalRevenue.observe(viewLifecycleOwner) {
            val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvRevenue.text = fmt.format(it ?: 0.0)
        }
        viewModel.inStockPhoneCount.observe(viewLifecycleOwner) {
            binding.tvInStockPhones.text = it.toString()
        }
        viewModel.warrantiesExpiringSoon.observe(viewLifecycleOwner) {
            binding.tvWarrantyAlert.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvWarrantyAlert.text = "⏰ ${it.size} warranty(s) expiring this week!"
        }

        // Load revenue chart data
        viewModel.loadDailyRevenue()
        viewModel.dailyRevenue.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.revenueChartContainer.visibility = View.VISIBLE
                // Simple bar chart drawn as text table (no library needed)
                val maxRev = data.maxOf { it.revenue }.takeIf { it > 0 } ?: 1.0
                val sb = StringBuilder()
                data.forEach { d ->
                    val bars = (d.revenue / maxRev * 10).toInt()
                    sb.append("${d.date}  ${"█".repeat(bars)}  ₹${String.format("%.0f", d.revenue)}\n")
                }
                binding.tvRevenueChart.text = sb.toString().trimEnd()
            }
        }

        // Navigation
        binding.cardCustomers.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_customers) }
        binding.cardJobs.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_jobs) }
        binding.cardParts.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_parts) }
        binding.cardBills.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_bills) }
        binding.cardInventory.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_inventory) }
        binding.cardTechnicians.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_technicians) }
        binding.btnNewJob.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_jobs) }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
