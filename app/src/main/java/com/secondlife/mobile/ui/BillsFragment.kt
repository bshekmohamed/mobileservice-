package com.secondlife.mobile.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.secondlife.mobile.databinding.FragmentBillsBinding
import java.text.NumberFormat
import java.util.Locale

class BillsFragment : Fragment() {
    private var _binding: FragmentBillsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: BillAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = BillAdapter()
        binding.recyclerBills.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBills.adapter = adapter

        viewModel.allBills.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.totalRevenue.observe(viewLifecycleOwner) {
            val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvTotalRevenue.text = "Total Revenue: ${fmt.format(it ?: 0.0)}"
        }
        viewModel.warrantiesExpiringSoon.observe(viewLifecycleOwner) {
            binding.tvWarrantyAlert.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvWarrantyAlert.text = "⏰ ${it.size} warranty(s) expiring this week!"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
