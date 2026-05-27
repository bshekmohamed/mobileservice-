package com.secondlife.mobile.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.secondlife.mobile.R
import com.secondlife.mobile.data.*
import com.secondlife.mobile.databinding.*

class RepairJobsFragment : Fragment() {
    private var _binding: FragmentJobsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: RepairJobAdapter
    private var customerList: List<Customer> = emptyList()
    private var technicianList: List<Technician> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RepairJobAdapter(
            viewModel = viewModel,
            onStatusChange = { job, status -> viewModel.updateRepairJob(job.copy(status = status)) },
            onBill = { job -> showBillDialog(job) },
            onWhatsApp = { jobWithCustomer ->
                val msg = viewModel.buildWhatsAppStatusMessage(jobWithCustomer)
                // BUG 12 FIX: ensure Indian format with country code 91
                val rawPhone = jobWithCustomer.customer.phone
                    .replace("+", "").replace(" ", "").replace("-", "")
                val phone = if (rawPhone.startsWith("91") && rawPhone.length >= 12) rawPhone
                            else if (rawPhone.length == 10) "91$rawPhone"
                            else rawPhone
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=${Uri.encode(msg)}"))
                startActivity(intent)
            },
            onDelete = { job ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Job")
                    .setMessage("Delete repair job for ${job.deviceBrand} ${job.deviceModel}?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteRepairJob(job) }
                    .setNegativeButton("Cancel", null).show()
            }
        )
        binding.recyclerJobs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerJobs.adapter = adapter

        viewModel.filteredJobs.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.allCustomers.observe(viewLifecycleOwner) { customerList = it }
        viewModel.allTechnicians.observe(viewLifecycleOwner) { technicianList = it }

        val statuses = listOf("All", "Received", "In Progress", "Completed", "Delivered")
        statuses.forEach { status ->
            val chip = Chip(requireContext()).apply {
                text = status; isCheckable = true; isChecked = status == "All"
                setOnClickListener { viewModel.filterJobsByStatus(status) }
            }
            binding.chipGroupStatus.addView(chip)
        }
        binding.fabAddJob.setOnClickListener { showAddJobDialog() }
    }

    private fun showAddJobDialog() {
        if (customerList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add a customer first", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogBinding = DialogRepairJobBinding.inflate(layoutInflater)

        // BUG 10 FIX: set adapter before setText; use post to prevent race on dropdown
        val customerNames = customerList.map { "${it.name} (${it.phone})" }.toTypedArray()
        val customerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, customerNames)
        dialogBinding.actvCustomer.setAdapter(customerAdapter)
        // BUG 10: track by index confirmed at click time
        var selectedCustomerIndex = 0
        dialogBinding.actvCustomer.setOnItemClickListener { _, _, position, _ ->
            selectedCustomerIndex = position
        }
        // Set default text after adapter attached — no race
        dialogBinding.actvCustomer.post {
            dialogBinding.actvCustomer.setText(customerNames[0], false)
        }

        val techNames = mutableListOf("No Technician") + technicianList.map { it.name }
        val techAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, techNames)
        techAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerTechnician.adapter = techAdapter

        val branches = arrayOf("Mimisal", "R.Puduppattinam")
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, branches)
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerBranch.adapter = branchAdapter

        // BUG 8 FIX: use show() with manual button override to prevent dialog dismiss on error
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Repair Job")
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null)  // null listener — we override below
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val brand = dialogBinding.etBrand.text.toString().trim()
                val model = dialogBinding.etModel.text.toString().trim()
                val issue = dialogBinding.etIssue.text.toString().trim()
                if (brand.isEmpty()) { dialogBinding.etBrand.error = "Required"; return@setOnClickListener }
                if (model.isEmpty()) { dialogBinding.etModel.error = "Required"; return@setOnClickListener }
                if (issue.isEmpty()) { dialogBinding.etIssue.error = "Required"; return@setOnClickListener }

                val selectedCustomerId = customerList[selectedCustomerIndex].id
                val techIdx = dialogBinding.spinnerTechnician.selectedItemPosition
                val techId = if (techIdx > 0) technicianList[techIdx - 1].id else 0
                val branch = branches[dialogBinding.spinnerBranch.selectedItemPosition]
                viewModel.saveRepairJob(RepairJob(
                    customerId = selectedCustomerId,
                    technicianId = techId,
                    deviceBrand = brand, deviceModel = model,
                    imei = dialogBinding.etImei.text.toString().trim(),
                    issueDescription = issue,
                    estimatedCost = dialogBinding.etEstimate.text.toString().toDoubleOrNull() ?: 0.0,
                    advancePaid = dialogBinding.etAdvance.text.toString().toDoubleOrNull() ?: 0.0,
                    branch = branch
                ))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showBillDialog(job: RepairJob) {
        val dialogBinding = DialogBillBinding.inflate(layoutInflater)
        dialogBinding.etLabor.setText(job.finalCost.toString())
        dialogBinding.etParts.setText("0")
        dialogBinding.etDiscount.setText("0")
        dialogBinding.etPaid.setText(job.advancePaid.toString())

        fun updateTotal() {
            val labor = dialogBinding.etLabor.text.toString().toDoubleOrNull() ?: 0.0
            val parts = dialogBinding.etParts.text.toString().toDoubleOrNull() ?: 0.0
            val discount = dialogBinding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
            dialogBinding.tvTotal.text = "Total: ₹${String.format("%.2f", labor + parts - discount)}"
        }
        updateTotal()
        listOf(dialogBinding.etLabor, dialogBinding.etParts, dialogBinding.etDiscount).forEach {
            it.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) = updateTotal()
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            })
        }

        // BUG 8 FIX: override positive button to prevent early dismiss
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Generate Bill")
            .setView(dialogBinding.root)
            .setPositiveButton("Generate", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val labor = dialogBinding.etLabor.text.toString().toDoubleOrNull() ?: 0.0
                val parts = dialogBinding.etParts.text.toString().toDoubleOrNull() ?: 0.0
                val discount = dialogBinding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
                val paid = dialogBinding.etPaid.text.toString().toDoubleOrNull() ?: 0.0
                val total = labor + parts - discount
                val payMode = when {
                    dialogBinding.rbCash.isChecked -> "Cash"
                    dialogBinding.rbUpi.isChecked -> "UPI"
                    else -> "Card"
                }
                val bill = Bill(
                    repairJobId = job.id,
                    billNumber = viewModel.generateBillNumber(),
                    laborCharge = labor, partsCharge = parts,
                    discount = discount, totalAmount = total,
                    amountPaid = paid, paymentMode = payMode,
                    branch = job.branch,
                    warrantyExpiry = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000)
                )
                viewModel.saveBill(bill)
                viewModel.updateRepairJob(job.copy(status = "Completed", finalCost = total))
                dialog.dismiss()
                Toast.makeText(requireContext(), "Bill generated!", Toast.LENGTH_SHORT).show()

                // BUG 3 FIX: null-safe customer lookup
                val jwc = adapter.currentList.firstOrNull { it.repairJob.id == job.id }
                if (jwc != null) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Share Bill")
                        .setMessage("Share bill as PDF?")
                        .setPositiveButton("WhatsApp") { _, _ ->
                            PdfBillHelper.shareViaWhatsApp(requireContext(), bill, job, jwc.customer)
                        }
                        .setNeutralButton("Other App") { _, _ ->
                            PdfBillHelper.generateAndShare(requireContext(), bill, job, jwc.customer)
                        }
                        .setNegativeButton("Skip", null).show()
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
