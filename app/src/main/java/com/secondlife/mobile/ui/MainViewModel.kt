package com.secondlife.mobile.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.secondlife.mobile.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val customerDao = db.customerDao()
    private val technicianDao = db.technicianDao()
    private val repairJobDao = db.repairJobDao()
    private val sparePartDao = db.sparePartDao()
    private val jobPartDao = db.jobPartDao()
    private val billDao = db.billDao()
    private val usedPhoneDao = db.usedPhoneDao()

    // ─── Branch state ──────────────────────────────────────────
    private val prefs = application.getSharedPreferences("slm_prefs", Context.MODE_PRIVATE)
    private val _activeBranch = MutableLiveData(prefs.getString("branch", "All") ?: "All")
    val activeBranch: LiveData<String> = _activeBranch
    fun setBranch(branch: String) {
        _activeBranch.value = branch
        prefs.edit().putString("branch", branch).apply()
    }

    // BUG 9 FIX: dashboard stats are now branch-aware via switchMap
    val customerCount: LiveData<Int> = _activeBranch.switchMap { branch ->
        if (branch == "All") customerDao.getCustomerCount()
        else customerDao.getCustomerCountByBranch(branch)
    }
    val activeJobCount: LiveData<Int> = _activeBranch.switchMap { branch ->
        if (branch == "All") repairJobDao.getActiveJobCount()
        else repairJobDao.getActiveJobCountByBranch(branch)
    }
    val lowStockCount: LiveData<Int> = _activeBranch.switchMap { branch ->
        if (branch == "All") sparePartDao.getLowStockCount()
        else sparePartDao.getLowStockCountByBranch(branch)
    }
    val totalRevenue: LiveData<Double?> = _activeBranch.switchMap { branch ->
        if (branch == "All") billDao.getTotalRevenue()
        else billDao.getTotalRevenueByBranch(branch)
    }
    val inStockPhoneCount: LiveData<Int> = usedPhoneDao.getInStockCount()

    // Warranty alerts
    val warrantiesExpiringSoon: LiveData<List<Bill>> get() {
        val now = System.currentTimeMillis()
        val soon = now + (7L * 24 * 60 * 60 * 1000)
        return billDao.getWarrantiesExpiringSoon(now, soon)
    }

    // Daily revenue (last 7 days)
    private val _dailyRevenue = MutableLiveData<List<DailyRevenue>>(emptyList())
    val dailyRevenue: LiveData<List<DailyRevenue>> = _dailyRevenue
    fun loadDailyRevenue() = viewModelScope.launch {
        val since = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        _dailyRevenue.value = billDao.getDailyRevenueSince(since)
    }

    // ─── Customers ────────────────────────────────────────────
    val allCustomers = customerDao.getAllCustomers()
    private val _customerSearch = MutableLiveData("")
    val searchedCustomers: LiveData<List<Customer>> = _customerSearch.switchMap {
        if (it.isBlank()) customerDao.getAllCustomers()
        else customerDao.searchCustomers(it)
    }
    fun searchCustomers(q: String) { _customerSearch.value = q }
    fun saveCustomer(c: Customer) = viewModelScope.launch { customerDao.insertCustomer(c) }
    fun deleteCustomer(c: Customer) = viewModelScope.launch { customerDao.deleteCustomer(c) }

    // ─── Technicians ──────────────────────────────────────────
    val allTechnicians = technicianDao.getAllTechnicians()
    fun saveTechnician(t: Technician) = viewModelScope.launch { technicianDao.insertTechnician(t) }
    fun deleteTechnician(t: Technician) = viewModelScope.launch { technicianDao.deleteTechnician(t) }

    // ─── Repair Jobs ──────────────────────────────────────────
    val allRepairJobs = repairJobDao.getAllRepairJobsWithCustomer()
    private val _jobStatusFilter = MutableLiveData("All")
    val filteredJobs: LiveData<List<RepairJobWithCustomer>> = _jobStatusFilter.switchMap {
        if (it == "All") repairJobDao.getAllRepairJobsWithCustomer()
        else repairJobDao.getRepairJobsByStatus(it)
    }
    fun filterJobsByStatus(status: String) { _jobStatusFilter.value = status }
    fun saveRepairJob(job: RepairJob) = viewModelScope.launch { repairJobDao.insertRepairJob(job) }
    fun updateRepairJob(job: RepairJob) = viewModelScope.launch {
        repairJobDao.updateRepairJob(job.copy(updatedAt = System.currentTimeMillis()))
    }
    fun deleteRepairJob(job: RepairJob) = viewModelScope.launch { repairJobDao.deleteRepairJob(job) }

    // ─── Parts for a job ──────────────────────────────────────
    fun addJobPart(jobPart: JobPart) = viewModelScope.launch {
        jobPartDao.insertJobPart(jobPart)
        val part = sparePartDao.getPartById(jobPart.sparePartId)
        part?.let {
            val newQty = (it.quantity - jobPart.quantity).coerceAtLeast(0)
            sparePartDao.updateSparePart(it.copy(quantity = newQty))
        }
    }
    fun removeJobPart(jobPart: JobPart) = viewModelScope.launch {
        jobPartDao.deleteJobPart(jobPart)
        val part = sparePartDao.getPartById(jobPart.sparePartId)
        part?.let { sparePartDao.updateSparePart(it.copy(quantity = it.quantity + jobPart.quantity)) }
    }
    fun getJobPartsLive(jobId: Int) = jobPartDao.getPartsByJobIdLive(jobId)

    // ─── Spare Parts ──────────────────────────────────────────
    val allSpareParts = sparePartDao.getAllSpareParts()
    val lowStockParts = sparePartDao.getLowStockParts()
    private val _partSearch = MutableLiveData("")
    val searchedParts: LiveData<List<SparePart>> = _partSearch.switchMap {
        if (it.isBlank()) sparePartDao.getAllSpareParts()
        else sparePartDao.searchSpareParts(it)
    }
    fun searchParts(q: String) { _partSearch.value = q }
    fun saveSparePart(p: SparePart) = viewModelScope.launch { sparePartDao.insertSparePart(p) }
    fun updateSparePart(p: SparePart) = viewModelScope.launch { sparePartDao.updateSparePart(p) }
    fun deleteSparePart(p: SparePart) = viewModelScope.launch { sparePartDao.deleteSparePart(p) }

    // ─── Bills ────────────────────────────────────────────────
    val allBills = billDao.getAllBills()
    fun saveBill(bill: Bill) = viewModelScope.launch { billDao.insertBill(bill) }

    fun generateBillNumber(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = sdf.format(Date())
        val random = (100..999).random()
        return "SLM-$date-$random"
    }

    // ─── Used Phones ──────────────────────────────────────────
    val allUsedPhones = usedPhoneDao.getAllUsedPhones()
    val inStockPhones = usedPhoneDao.getInStockPhones()
    private val _phoneSearch = MutableLiveData("")
    val searchedPhones: LiveData<List<UsedPhone>> = _phoneSearch.switchMap {
        if (it.isBlank()) usedPhoneDao.getAllUsedPhones()
        else usedPhoneDao.searchUsedPhones(it)
    }
    fun searchPhones(q: String) { _phoneSearch.value = q }
    fun saveUsedPhone(p: UsedPhone) = viewModelScope.launch { usedPhoneDao.insertUsedPhone(p) }
    fun updateUsedPhone(p: UsedPhone) = viewModelScope.launch { usedPhoneDao.updateUsedPhone(p) }
    fun deleteUsedPhone(p: UsedPhone) = viewModelScope.launch { usedPhoneDao.deleteUsedPhone(p) }
    fun sellPhone(phone: UsedPhone, buyerId: Int?) = viewModelScope.launch {
        usedPhoneDao.updateUsedPhone(
            phone.copy(status = "Sold", soldAt = System.currentTimeMillis(), buyerId = buyerId)
        )
    }

    // ─── Helpers ──────────────────────────────────────────────
    fun getStatusColor(status: String): Int {
        return when (status) {
            "Received"    -> android.graphics.Color.parseColor("#FF9800")
            "In Progress" -> android.graphics.Color.parseColor("#2196F3")
            "Completed"   -> android.graphics.Color.parseColor("#4CAF50")
            "Delivered"   -> android.graphics.Color.parseColor("#9E9E9E")
            else          -> android.graphics.Color.parseColor("#607D8B")
        }
    }

    fun getGradeColor(grade: String): Int {
        return when (grade) {
            "A+" -> android.graphics.Color.parseColor("#1B5E20")
            "A"  -> android.graphics.Color.parseColor("#2E7D32")
            "B+" -> android.graphics.Color.parseColor("#E65100")
            "B"  -> android.graphics.Color.parseColor("#B71C1C")
            else -> android.graphics.Color.parseColor("#607D8B")
        }
    }

    fun buildWhatsAppStatusMessage(job: RepairJobWithCustomer): String {
        return """*Second Life Mobiles* 📱
Hello ${job.customer.name}!

Your *${job.repairJob.deviceBrand} ${job.repairJob.deviceModel}* repair update:
Status: *${job.repairJob.status}*
Job ID: #${job.repairJob.id}

${if (job.repairJob.status == "Completed") "Your device is ready for pickup! 🎉" else "We will notify you when it's ready."}

_90-day warranty on all repairs_
Tested. Trusted. Twice as Smart. ✅"""
    }

    fun buildWhatsAppSaleMessage(phone: UsedPhone): String {
        return """*Second Life Mobiles* 📱
📢 Available: *${phone.brand} ${phone.model}*
Grade: *${phone.grade}* | Storage: ${phone.storage} | RAM: ${phone.ram}
Color: ${phone.color}
Price: *₹${phone.sellingPrice}*

90-day warranty included ✅
Tested. Trusted. Twice as Smart."""
    }
}
