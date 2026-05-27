package com.secondlife.mobile.data

import androidx.lifecycle.LiveData
import androidx.room.*

// ─── Customer DAO ──────────────────────────────────────────────
@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE branch = :branch ORDER BY createdAt DESC")
    fun getCustomersByBranch(branch: String): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Int): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE branch = :branch")
    fun getCustomerCountByBranch(branch: String): LiveData<Int>

    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    suspend fun getAllCustomersList(): List<Customer>
}

// ─── Technician DAO ────────────────────────────────────────────
@Dao
interface TechnicianDao {
    @Query("SELECT * FROM technicians WHERE isActive = 1 ORDER BY name ASC")
    fun getAllTechnicians(): LiveData<List<Technician>>

    @Query("SELECT * FROM technicians WHERE branch = :branch AND isActive = 1")
    fun getTechniciansByBranch(branch: String): LiveData<List<Technician>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnician(technician: Technician): Long

    @Update
    suspend fun updateTechnician(technician: Technician)

    @Delete
    suspend fun deleteTechnician(technician: Technician)

    @Query("SELECT COUNT(*) FROM repair_jobs WHERE technicianId = :techId AND status = 'Delivered'")
    suspend fun getCompletedJobCount(techId: Int): Int

    @Query("SELECT * FROM technicians ORDER BY name ASC")
    suspend fun getAllTechniciansList(): List<Technician>
}

// ─── RepairJob DAO ─────────────────────────────────────────────
@Dao
interface RepairJobDao {
    @Transaction
    @Query("SELECT * FROM repair_jobs ORDER BY createdAt DESC")
    fun getAllRepairJobsWithCustomer(): LiveData<List<RepairJobWithCustomer>>

    @Transaction
    @Query("SELECT * FROM repair_jobs WHERE status = :status ORDER BY createdAt DESC")
    fun getRepairJobsByStatus(status: String): LiveData<List<RepairJobWithCustomer>>

    @Transaction
    @Query("SELECT * FROM repair_jobs WHERE branch = :branch ORDER BY createdAt DESC")
    fun getRepairJobsByBranch(branch: String): LiveData<List<RepairJobWithCustomer>>

    @Transaction
    @Query("SELECT * FROM repair_jobs WHERE branch = :branch AND status = :status ORDER BY createdAt DESC")
    fun getRepairJobsByBranchAndStatus(branch: String, status: String): LiveData<List<RepairJobWithCustomer>>

    @Transaction
    @Query("SELECT * FROM repair_jobs WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getRepairJobsByCustomer(customerId: Int): LiveData<List<RepairJobWithCustomer>>

    @Query("SELECT * FROM repair_jobs WHERE id = :id")
    suspend fun getRepairJobById(id: Int): RepairJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepairJob(repairJob: RepairJob): Long

    @Update
    suspend fun updateRepairJob(repairJob: RepairJob)

    @Delete
    suspend fun deleteRepairJob(repairJob: RepairJob)

    @Query("SELECT COUNT(*) FROM repair_jobs WHERE status != 'Delivered'")
    fun getActiveJobCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM repair_jobs WHERE status != 'Delivered' AND branch = :branch")
    fun getActiveJobCountByBranch(branch: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM repair_jobs WHERE status = :status")
    fun getCountByStatus(status: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM repair_jobs WHERE technicianId = :techId AND status != 'Delivered'")
    fun getActiveTechJobCount(techId: Int): LiveData<Int>

    @Query("SELECT * FROM repair_jobs ORDER BY createdAt DESC")
    suspend fun getAllRepairJobsList(): List<RepairJob>
}

// ─── SparePart DAO ─────────────────────────────────────────────
@Dao
interface SparePartDao {
    @Query("SELECT * FROM spare_parts ORDER BY name ASC")
    fun getAllSpareParts(): LiveData<List<SparePart>>

    @Query("SELECT * FROM spare_parts WHERE branch = :branch ORDER BY name ASC")
    fun getSparePartsByBranch(branch: String): LiveData<List<SparePart>>

    @Query("SELECT * FROM spare_parts WHERE quantity <= minStock ORDER BY quantity ASC")
    fun getLowStockParts(): LiveData<List<SparePart>>

    @Query("SELECT * FROM spare_parts WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchSpareParts(query: String): LiveData<List<SparePart>>

    @Query("SELECT * FROM spare_parts WHERE id = :id")
    suspend fun getPartById(id: Int): SparePart?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSparePart(sparePart: SparePart): Long

    @Update
    suspend fun updateSparePart(sparePart: SparePart)

    @Delete
    suspend fun deleteSparePart(sparePart: SparePart)

    @Query("SELECT COUNT(*) FROM spare_parts WHERE quantity <= minStock")
    fun getLowStockCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM spare_parts WHERE quantity <= minStock AND branch = :branch")
    fun getLowStockCountByBranch(branch: String): LiveData<Int>

    @Query("SELECT * FROM spare_parts ORDER BY name ASC")
    suspend fun getAllSparePartsList(): List<SparePart>
}

// ─── JobPart DAO ───────────────────────────────────────────────
@Dao
interface JobPartDao {
    @Query("SELECT * FROM job_parts WHERE repairJobId = :jobId")
    suspend fun getPartsByJobId(jobId: Int): List<JobPart>

    @Query("SELECT * FROM job_parts WHERE repairJobId = :jobId")
    fun getPartsByJobIdLive(jobId: Int): LiveData<List<JobPart>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobPart(jobPart: JobPart): Long

    @Delete
    suspend fun deleteJobPart(jobPart: JobPart)

    @Query("DELETE FROM job_parts WHERE repairJobId = :jobId")
    suspend fun deleteAllPartsForJob(jobId: Int)
}

// ─── Bill DAO ──────────────────────────────────────────────────
@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    fun getAllBills(): LiveData<List<Bill>>

    @Query("SELECT * FROM bills WHERE branch = :branch ORDER BY createdAt DESC")
    fun getBillsByBranch(branch: String): LiveData<List<Bill>>

    @Query("SELECT * FROM bills WHERE repairJobId = :jobId")
    suspend fun getBillByJobId(jobId: Int): Bill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Update
    suspend fun updateBill(bill: Bill)

    @Query("SELECT SUM(totalAmount) FROM bills")
    fun getTotalRevenue(): LiveData<Double?>

    @Query("SELECT SUM(totalAmount) FROM bills WHERE branch = :branch")
    fun getTotalRevenueByBranch(branch: String): LiveData<Double?>

    @Query("SELECT COUNT(*) FROM bills")
    fun getBillCount(): LiveData<Int>

    @Query("""SELECT strftime('%d %b', datetime(createdAt/1000, 'unixepoch', 'localtime')) as date,
              SUM(totalAmount) as revenue
              FROM bills
              WHERE createdAt >= :since
              GROUP BY date ORDER BY createdAt ASC""")
    suspend fun getDailyRevenueSince(since: Long): List<DailyRevenue>

    @Query("""SELECT * FROM bills WHERE warrantyExpiry >= :now AND warrantyExpiry <= :soon""")
    fun getWarrantiesExpiringSoon(now: Long, soon: Long): LiveData<List<Bill>>

    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    suspend fun getAllBillsList(): List<Bill>
}

// ─── UsedPhone DAO ─────────────────────────────────────────────
@Dao
interface UsedPhoneDao {
    @Query("SELECT * FROM used_phones ORDER BY createdAt DESC")
    fun getAllUsedPhones(): LiveData<List<UsedPhone>>

    @Query("SELECT * FROM used_phones WHERE branch = :branch ORDER BY createdAt DESC")
    fun getUsedPhonesByBranch(branch: String): LiveData<List<UsedPhone>>

    @Query("SELECT * FROM used_phones WHERE status = 'In Stock' ORDER BY createdAt DESC")
    fun getInStockPhones(): LiveData<List<UsedPhone>>

    @Query("SELECT * FROM used_phones WHERE brand LIKE '%'||:q||'%' OR model LIKE '%'||:q||'%' OR grade LIKE '%'||:q||'%'")
    fun searchUsedPhones(q: String): LiveData<List<UsedPhone>>

    @Query("SELECT COUNT(*) FROM used_phones WHERE status = 'In Stock'")
    fun getInStockCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsedPhone(phone: UsedPhone): Long

    @Update
    suspend fun updateUsedPhone(phone: UsedPhone)

    @Delete
    suspend fun deleteUsedPhone(phone: UsedPhone)

    @Query("SELECT * FROM used_phones ORDER BY createdAt DESC")
    suspend fun getAllUsedPhonesList(): List<UsedPhone>
}

// ─── Backup-only suspend queries (added for BackupRestoreHelper Bug 7 fix) ──
