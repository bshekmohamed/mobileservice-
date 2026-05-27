package com.secondlife.mobile.ui

import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.secondlife.mobile.R
import com.secondlife.mobile.data.*
import com.secondlife.mobile.databinding.*
import java.text.SimpleDateFormat
import java.util.*

// ─── Customer Adapter ──────────────────────────────────────────
class CustomerAdapter(
    private val onEdit: (Customer) -> Unit,
    private val onDelete: (Customer) -> Unit
) : ListAdapter<Customer, CustomerAdapter.VH>(object : DiffUtil.ItemCallback<Customer>() {
    override fun areItemsTheSame(a: Customer, b: Customer) = a.id == b.id
    override fun areContentsTheSame(a: Customer, b: Customer) = a == b
}) {
    inner class VH(val binding: ItemCustomerBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = getItem(position)
        holder.binding.apply {
            tvName.text = c.name
            tvPhone.text = c.phone
            tvEmail.text = if (c.email.isNotEmpty()) c.email else "No email"
            tvAddress.text = if (c.address.isNotEmpty()) c.address else "No address"
            tvBranch.text = c.branch
            btnWhatsApp.setOnClickListener {
                val phone = c.phone.replace("+", "").replace(" ", "")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone"))
                it.context.startActivity(intent)
            }
            btnMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(R.menu.menu_item, menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> { onEdit(c); true }
                            R.id.action_delete -> { onDelete(c); true }
                            else -> false
                        }
                    }
                    show()
                }
            }
        }
    }
}

// ─── RepairJob Adapter ─────────────────────────────────────────
class RepairJobAdapter(
    private val viewModel: MainViewModel,
    private val onStatusChange: (RepairJob, String) -> Unit,
    private val onBill: (RepairJob) -> Unit,
    private val onWhatsApp: (RepairJobWithCustomer) -> Unit,
    private val onDelete: (RepairJob) -> Unit
) : ListAdapter<RepairJobWithCustomer, RepairJobAdapter.VH>(object : DiffUtil.ItemCallback<RepairJobWithCustomer>() {
    override fun areItemsTheSame(a: RepairJobWithCustomer, b: RepairJobWithCustomer) = a.repairJob.id == b.repairJob.id
    override fun areContentsTheSame(a: RepairJobWithCustomer, b: RepairJobWithCustomer) = a == b
}) {
    inner class VH(val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val job = item.repairJob
        val customer = item.customer
        holder.binding.apply {
            tvDevice.text = "${job.deviceBrand} ${job.deviceModel}"
            tvCustomer.text = customer.name
            tvPhone.text = customer.phone
            tvIssue.text = job.issueDescription
            tvStatus.text = job.status
            tvStatus.setBackgroundColor(viewModel.getStatusColor(job.status))
            tvCost.text = "Est: ₹${job.estimatedCost}  Adv: ₹${job.advancePaid}"
            tvBranch.text = job.branch
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvDate.text = sdf.format(Date(job.createdAt))

            btnChangeStatus.setOnClickListener {
                val statuses = arrayOf("Received", "In Progress", "Completed", "Delivered")
                PopupMenu(it.context, it).apply {
                    statuses.forEach { s -> menu.add(s) }
                    setOnMenuItemClickListener { mi -> onStatusChange(job, mi.title.toString()); true }
                    show()
                }
            }
            btnGenerateBill.setOnClickListener { onBill(job) }
            btnWhatsAppStatus.setOnClickListener { onWhatsApp(item) }
            btnDelete.setOnClickListener { onDelete(job) }
        }
    }
}

// ─── SparePart Adapter ─────────────────────────────────────────
class SparePartAdapter(
    private val onEdit: (SparePart) -> Unit,
    private val onDelete: (SparePart) -> Unit,
    private val onStockUpdate: (SparePart, Int) -> Unit
) : ListAdapter<SparePart, SparePartAdapter.VH>(object : DiffUtil.ItemCallback<SparePart>() {
    override fun areItemsTheSame(a: SparePart, b: SparePart) = a.id == b.id
    override fun areContentsTheSame(a: SparePart, b: SparePart) = a == b
}) {
    inner class VH(val binding: ItemPartBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPartBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = getItem(position)
        holder.binding.apply {
            tvPartName.text = p.name
            tvCategory.text = p.category
            tvCompatible.text = if (p.compatibleModels.isNotEmpty()) p.compatibleModels else "All models"
            tvQuantity.text = "Qty: ${p.quantity}"
            tvQuantity.setTextColor(if (p.quantity <= p.minStock) android.graphics.Color.parseColor("#F87171") else android.graphics.Color.parseColor("#22C55E"))
            tvPrice.text = "Buy: ₹${p.buyingPrice}  Sell: ₹${p.sellingPrice}"
            tvBranch.text = p.branch
            btnAdd.setOnClickListener { onStockUpdate(p, 1) }
            btnRemove.setOnClickListener { onStockUpdate(p, -1) }
            btnMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(R.menu.menu_item, menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> { onEdit(p); true }
                            R.id.action_delete -> { onDelete(p); true }
                            else -> false
                        }
                    }
                    show()
                }
            }
        }
    }
}

// ─── Bill Adapter ──────────────────────────────────────────────
class BillAdapter : ListAdapter<Bill, BillAdapter.VH>(object : DiffUtil.ItemCallback<Bill>() {
    override fun areItemsTheSame(a: Bill, b: Bill) = a.id == b.id
    override fun areContentsTheSame(a: Bill, b: Bill) = a == b
}) {
    inner class VH(val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = getItem(position)
        holder.binding.apply {
            tvBillNumber.text = b.billNumber
            tvLabor.text = "Labour: ₹${b.laborCharge}"
            tvParts.text = "Parts: ₹${b.partsCharge}"
            tvDiscount.text = "Discount: ₹${b.discount}"
            tvTotal.text = "Total: ₹${b.totalAmount}"
            tvPaid.text = "Paid: ₹${b.amountPaid}"
            tvBalance.text = "Balance: ₹${b.totalAmount - b.amountPaid}"
            tvPayMode.text = b.paymentMode
            tvBranch.text = b.branch
            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
            tvDate.text = sdf.format(Date(b.createdAt))
            val sdfW = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvWarranty.text = "Warranty till: ${sdfW.format(Date(b.warrantyExpiry))}"
            val balance = b.totalAmount - b.amountPaid
            tvBalance.setTextColor(if (balance > 0) android.graphics.Color.parseColor("#F87171") else android.graphics.Color.parseColor("#22C55E"))
        }
    }
}

// ─── Technician Adapter ────────────────────────────────────────
class TechnicianAdapter(
    private val onEdit: (Technician) -> Unit,
    private val onDelete: (Technician) -> Unit
) : ListAdapter<Technician, TechnicianAdapter.VH>(object : DiffUtil.ItemCallback<Technician>() {
    override fun areItemsTheSame(a: Technician, b: Technician) = a.id == b.id
    override fun areContentsTheSame(a: Technician, b: Technician) = a == b
}) {
    inner class VH(val binding: ItemTechnicianBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTechnicianBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = getItem(position)
        holder.binding.apply {
            tvTechName.text = t.name
            tvTechPhone.text = if (t.phone.isNotEmpty()) t.phone else "No phone"
            tvTechSpecialty.text = if (t.specialty.isNotEmpty()) t.specialty else "General"
            tvTechBranch.text = t.branch
            btnTechMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(R.menu.menu_item, menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> { onEdit(t); true }
                            R.id.action_delete -> { onDelete(t); true }
                            else -> false
                        }
                    }
                    show()
                }
            }
        }
    }
}

// ─── UsedPhone Adapter ─────────────────────────────────────────
class UsedPhoneAdapter(
    private val viewModel: MainViewModel,
    private val onEdit: (UsedPhone) -> Unit,
    private val onSell: (UsedPhone) -> Unit,
    private val onWhatsApp: (UsedPhone) -> Unit,
    private val onDelete: (UsedPhone) -> Unit
) : ListAdapter<UsedPhone, UsedPhoneAdapter.VH>(object : DiffUtil.ItemCallback<UsedPhone>() {
    override fun areItemsTheSame(a: UsedPhone, b: UsedPhone) = a.id == b.id
    override fun areContentsTheSame(a: UsedPhone, b: UsedPhone) = a == b
}) {
    inner class VH(val binding: ItemUsedPhoneBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemUsedPhoneBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = getItem(position)
        holder.binding.apply {
            tvPhoneName.text = "${p.brand} ${p.model}"
            tvGrade.text = p.grade
            tvGrade.setTextColor(viewModel.getGradeColor(p.grade))
            tvPhoneSpecs.text = "${p.storage} / ${p.ram} · ${p.color}"
            tvSellPrice.text = "Sell: ₹${p.sellingPrice}"
            tvBuyPrice.text = "Buy: ₹${p.buyingPrice}"
            tvStatusPhone.text = p.status
            tvStatusPhone.setTextColor(when (p.status) {
                "In Stock" -> android.graphics.Color.parseColor("#22C55E")
                "Sold" -> android.graphics.Color.parseColor("#6B7280")
                else -> android.graphics.Color.parseColor("#F59E0B")
            })
            btnSellPhone.visibility = if (p.status == "In Stock") View.VISIBLE else View.GONE
            btnSellPhone.setOnClickListener { onSell(p) }
            btnEditPhone.setOnClickListener { onEdit(p) }
            btnWhatsappPhone.setOnClickListener { onWhatsApp(p) }
            btnDeletePhone.setOnClickListener { onDelete(p) }
        }
    }
}
