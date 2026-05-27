package com.secondlife.mobile.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.secondlife.mobile.data.Technician
import com.secondlife.mobile.databinding.FragmentTechniciansBinding
import com.secondlife.mobile.databinding.DialogTechnicianBinding

class TechniciansFragment : Fragment() {
    private var _binding: FragmentTechniciansBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: TechnicianAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTechniciansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TechnicianAdapter(
            onEdit = { showDialog(it) },
            onDelete = { t ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove Technician")
                    .setMessage("Remove ${t.name}?")
                    .setPositiveButton("Remove") { _, _ -> viewModel.deleteTechnician(t) }
                    .setNegativeButton("Cancel", null).show()
            }
        )
        binding.recyclerTechnicians.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTechnicians.adapter = adapter
        viewModel.allTechnicians.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        binding.fabAddTechnician.setOnClickListener { showDialog(null) }
    }

    private fun showDialog(tech: Technician?) {
        val d = DialogTechnicianBinding.inflate(layoutInflater)
        val branches = arrayOf("Mimisal", "R.Puduppattinam")

        // BUG 4 FIX: set adapter FIRST, then setSelection
        val spinnerAdapter = android.widget.ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, branches
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        d.spinnerTechBranch.adapter = spinnerAdapter

        // Now safe to pre-fill edit fields including setSelection
        tech?.let {
            d.etTechName.setText(it.name)
            d.etTechPhone.setText(it.phone)
            d.etTechSpecialty.setText(it.specialty)
            val idx = branches.indexOf(it.branch).coerceAtLeast(0)
            d.spinnerTechBranch.setSelection(idx)
        }

        // BUG 8 FIX: override positive button to keep dialog open on validation fail
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (tech == null) "Add Technician" else "Edit Technician")
            .setView(d.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = d.etTechName.text.toString().trim()
                if (name.isEmpty()) {
                    d.etTechName.error = "Name required"
                    return@setOnClickListener
                }
                viewModel.saveTechnician(Technician(
                    id = tech?.id ?: 0,
                    name = name,
                    phone = d.etTechPhone.text.toString().trim(),
                    specialty = d.etTechSpecialty.text.toString().trim(),
                    branch = branches[d.spinnerTechBranch.selectedItemPosition]
                ))
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
