package com.secondlife.mobile.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.secondlife.mobile.data.Customer
import com.secondlife.mobile.databinding.DialogCustomerBinding
import com.secondlife.mobile.databinding.FragmentCustomersBinding

class CustomersFragment : Fragment() {
    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: CustomerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CustomerAdapter(
            onEdit = { showCustomerDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.recyclerCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCustomers.adapter = adapter

        viewModel.searchedCustomers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean { viewModel.searchCustomers(q ?: ""); return true }
        })
        binding.fabAddCustomer.setOnClickListener { showCustomerDialog(null) }
    }

    private fun showCustomerDialog(customer: Customer?) {
        val d = DialogCustomerBinding.inflate(layoutInflater)
        val branches = arrayOf("Mimisal", "R.Puduppattinam")
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, branches)
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        d.spinnerCustomerBranch.adapter = branchAdapter  // adapter set FIRST

        customer?.let {
            d.etName.setText(it.name)
            d.etPhone.setText(it.phone)
            d.etEmail.setText(it.email)
            d.etAddress.setText(it.address)
            d.spinnerCustomerBranch.setSelection(branches.indexOf(it.branch).coerceAtLeast(0))
        }

        // BUG 8 FIX: override positive button to keep dialog open on error
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (customer == null) "Add Customer" else "Edit Customer")
            .setView(d.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = d.etName.text.toString().trim()
                val phone = d.etPhone.text.toString().trim()
                if (name.isEmpty()) { d.etName.error = "Required"; return@setOnClickListener }
                if (phone.isEmpty()) { d.etPhone.error = "Required"; return@setOnClickListener }

                // BUG 6 FIX: preserve createdAt on edit; don't overwrite with current time
                viewModel.saveCustomer(Customer(
                    id = customer?.id ?: 0,
                    name = name,
                    phone = phone,
                    email = d.etEmail.text.toString().trim(),
                    address = d.etAddress.text.toString().trim(),
                    branch = branches[d.spinnerCustomerBranch.selectedItemPosition],
                    createdAt = customer?.createdAt ?: System.currentTimeMillis() // BUG 6 FIX
                ))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmDelete(customer: Customer) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Customer")
            .setMessage("Delete ${customer.name}? All repair jobs will also be deleted.")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteCustomer(customer) }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
