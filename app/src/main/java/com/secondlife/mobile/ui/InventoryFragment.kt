package com.secondlife.mobile.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.secondlife.mobile.data.UsedPhone
import com.secondlife.mobile.databinding.FragmentInventoryBinding
import com.secondlife.mobile.databinding.DialogUsedPhoneBinding

class InventoryFragment : Fragment() {
    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: UsedPhoneAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UsedPhoneAdapter(
            viewModel = viewModel,
            onEdit = { showDialog(it) },
            onSell = { phone -> showSellDialog(phone) },
            onWhatsApp = { phone ->
                val msg = viewModel.buildWhatsAppSaleMessage(phone)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=${Uri.encode(msg)}"))
                startActivity(intent)
            },
            onDelete = { phone ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Phone")
                    .setMessage("Delete ${phone.brand} ${phone.model}?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteUsedPhone(phone) }
                    .setNegativeButton("Cancel", null).show()
            }
        )
        binding.recyclerInventory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInventory.adapter = adapter

        viewModel.searchedPhones.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.inStockPhoneCount.observe(viewLifecycleOwner) {
            binding.tvInStockCount.text = "In Stock: $it phones"
        }

        binding.searchViewInventory.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean { viewModel.searchPhones(q ?: ""); return true }
        })
        binding.fabAddPhone.setOnClickListener { showDialog(null) }
    }

    private fun showDialog(phone: UsedPhone?) {
        val d = DialogUsedPhoneBinding.inflate(layoutInflater)
        val grades = arrayOf("A+", "A", "B+", "B")
        val gradeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, grades)
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        d.spinnerGrade.adapter = gradeAdapter

        val branches = arrayOf("Mimisal", "R.Puduppattinam")
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, branches)
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        d.spinnerPhoneBranch.adapter = branchAdapter

        phone?.let {
            d.etPhoneBrand.setText(it.brand)
            d.etPhoneModel.setText(it.model)
            d.etPhoneImei.setText(it.imei)
            d.etStorage.setText(it.storage)
            d.etRam.setText(it.ram)
            d.etPhoneColor.setText(it.color)
            d.etConditionNotes.setText(it.condition)
            d.etBuyPrice.setText(it.buyingPrice.toString())
            d.etSellPrice.setText(it.sellingPrice.toString())
            d.spinnerGrade.setSelection(grades.indexOf(it.grade).coerceAtLeast(0))
            d.spinnerPhoneBranch.setSelection(branches.indexOf(it.branch).coerceAtLeast(0))
        }

        // BUG 8 FIX: override positive button
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (phone == null) "Add Used Phone" else "Edit Phone")
            .setView(d.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val brand = d.etPhoneBrand.text.toString().trim()
                val model = d.etPhoneModel.text.toString().trim()
                if (brand.isEmpty()) { d.etPhoneBrand.error = "Required"; return@setOnClickListener }
                if (model.isEmpty()) { d.etPhoneModel.error = "Required"; return@setOnClickListener }

                // BUG 5 FIX: preserve soldAt and buyerId from original phone on edit
                viewModel.saveUsedPhone(UsedPhone(
                    id = phone?.id ?: 0,
                    brand = brand, model = model,
                    imei = d.etPhoneImei.text.toString().trim(),
                    grade = grades[d.spinnerGrade.selectedItemPosition],
                    storage = d.etStorage.text.toString().trim(),
                    ram = d.etRam.text.toString().trim(),
                    color = d.etPhoneColor.text.toString().trim(),
                    condition = d.etConditionNotes.text.toString().trim(),
                    buyingPrice = d.etBuyPrice.text.toString().toDoubleOrNull() ?: 0.0,
                    sellingPrice = d.etSellPrice.text.toString().toDoubleOrNull() ?: 0.0,
                    branch = branches[d.spinnerPhoneBranch.selectedItemPosition],
                    status = phone?.status ?: "In Stock",
                    soldAt = phone?.soldAt,     // BUG 5 FIX: preserved
                    buyerId = phone?.buyerId,   // BUG 5 FIX: preserved
                    createdAt = phone?.createdAt ?: System.currentTimeMillis() // BUG 6 pattern fix
                ))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showSellDialog(phone: UsedPhone) {
        val customers = viewModel.allCustomers.value ?: emptyList()
        if (customers.isEmpty()) {
            viewModel.sellPhone(phone, null)
            Toast.makeText(requireContext(), "Marked as Sold", Toast.LENGTH_SHORT).show()
            return
        }
        val names = arrayOf("Walk-in Customer") + customers.map { "${it.name} (${it.phone})" }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sell: ${phone.brand} ${phone.model}")
            .setItems(names) { _, idx ->
                val buyerId = if (idx == 0) null else customers[idx - 1].id
                viewModel.sellPhone(phone, buyerId)
                Toast.makeText(requireContext(), "Sold! ✅", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
