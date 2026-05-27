package com.secondlife.mobile.ui

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.secondlife.mobile.data.SparePart
import com.secondlife.mobile.databinding.DialogSparePartBinding
import com.secondlife.mobile.databinding.FragmentPartsBinding

class SparePartsFragment : Fragment() {
    private var _binding: FragmentPartsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: SparePartAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SparePartAdapter(
            onEdit = { showPartDialog(it) },
            onDelete = { part ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Part").setMessage("Delete ${part.name}?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteSparePart(part) }
                    .setNegativeButton("Cancel", null).show()
            },
            onStockUpdate = { part, qty ->
                viewModel.updateSparePart(part.copy(quantity = (part.quantity + qty).coerceAtLeast(0)))
            }
        )
        binding.recyclerParts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerParts.adapter = adapter

        viewModel.searchedParts.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.lowStockParts.observe(viewLifecycleOwner) {
            binding.tvLowStockAlert.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvLowStockAlert.text = "⚠️ ${it.size} item(s) low on stock!"
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean { viewModel.searchParts(q ?: ""); return true }
        })
        binding.fabAddPart.setOnClickListener { showPartDialog(null) }
    }

    private fun showPartDialog(part: SparePart?) {
        val d = DialogSparePartBinding.inflate(layoutInflater)
        val branches = arrayOf("Mimisal", "R.Puduppattinam")
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, branches)
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        d.spinnerPartBranch.adapter = branchAdapter

        part?.let {
            d.etPartName.setText(it.name)
            d.etCategory.setText(it.category)
            d.etCompatible.setText(it.compatibleModels)
            d.etQuantity.setText(it.quantity.toString())
            d.etBuyPrice.setText(it.buyingPrice.toString())
            d.etSellPrice.setText(it.sellingPrice.toString())
            d.etMinStock.setText(it.minStock.toString())
            d.spinnerPartBranch.setSelection(branches.indexOf(it.branch).coerceAtLeast(0))
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (part == null) "Add Spare Part" else "Edit Spare Part")
            .setView(d.root)
            .setPositiveButton("Save") { _, _ ->
                val name = d.etPartName.text.toString().trim()
                val category = d.etCategory.text.toString().trim()
                if (name.isEmpty() || category.isEmpty()) {
                    Toast.makeText(requireContext(), "Name and Category required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.saveSparePart(SparePart(
                    id = part?.id ?: 0, name = name, category = category,
                    compatibleModels = d.etCompatible.text.toString().trim(),
                    quantity = d.etQuantity.text.toString().toIntOrNull() ?: 0,
                    buyingPrice = d.etBuyPrice.text.toString().toDoubleOrNull() ?: 0.0,
                    sellingPrice = d.etSellPrice.text.toString().toDoubleOrNull() ?: 0.0,
                    minStock = d.etMinStock.text.toString().toIntOrNull() ?: 2,
                    branch = branches[d.spinnerPartBranch.selectedItemPosition]
                ))
            }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
