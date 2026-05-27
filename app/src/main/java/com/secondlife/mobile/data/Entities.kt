package com.secondlife.mobile.data

import androidx.room.*

// ─── Customer Entity ───────────────────────────────────────────
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val branch: String = "Mimisal",
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Technician Entity ─────────────────────────────────────────
@Entity(tableName = "technicians")
data class Technician(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val specialty: String = "",
    val branch: String = "Mimisal",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Repair Job Entity ─────────────────────────────────────────
@Entity(tableName = "repair_jobs", foreignKeys = [
    ForeignKey(entity = Customer::class, parentColumns = ["id"],
        childColumns = ["customerId"], onDelete = ForeignKey.CASCADE)
])
data class RepairJob(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val technicianId: Int = 0,
    val deviceBrand: String,
    val deviceModel: String,
    val imei: String = "",
    val issueDescription: String,
    val status: String = "Received", // Received, In Progress, Completed, Delivered
    val estimatedCost: Double = 0.0,
    val finalCost: Double = 0.0,
    val advancePaid: Double = 0.0,
    val technicianNotes: String = "",
    val branch: String = "Mimisal",
    val warrantyDays: Int = 90,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ─── Spare Part Entity ─────────────────────────────────────────
@Entity(tableName = "spare_parts")
data class SparePart(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val compatibleModels: String = "",
    val quantity: Int = 0,
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val minStock: Int = 2,
    val branch: String = "Mimisal",
    val updatedAt: Long = System.currentTimeMillis()
)

// ─── Job Parts join Entity ─────────────────────────────────────
@Entity(
    tableName = "job_parts",
    foreignKeys = [
        ForeignKey(entity = RepairJob::class, parentColumns = ["id"],
            childColumns = ["repairJobId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SparePart::class, parentColumns = ["id"],
            childColumns = ["sparePartId"], onDelete = ForeignKey.SET_DEFAULT)
    ]
)
data class JobPart(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val repairJobId: Int,
    val sparePartId: Int,
    val partName: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0
)

// ─── Bill Entity ───────────────────────────────────────────────
@Entity(tableName = "bills", foreignKeys = [
    ForeignKey(entity = RepairJob::class, parentColumns = ["id"],
        childColumns = ["repairJobId"], onDelete = ForeignKey.CASCADE)
])
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val repairJobId: Int,
    val billNumber: String,
    val laborCharge: Double = 0.0,
    val partsCharge: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val paymentMode: String = "Cash",
    val branch: String = "Mimisal",
    val warrantyExpiry: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Used Phone (Second-Hand Inventory) ───────────────────────
@Entity(tableName = "used_phones")
data class UsedPhone(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brand: String,
    val model: String,
    val imei: String = "",
    val grade: String = "A",         // A+, A, B+, B
    val storage: String = "",        // e.g. "128GB"
    val ram: String = "",            // e.g. "6GB"
    val color: String = "",
    val condition: String = "",      // cosmetic notes
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val status: String = "In Stock", // In Stock, Sold, Reserved
    val branch: String = "Mimisal",
    val soldAt: Long? = null,
    val buyerId: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Daily Revenue for charts ──────────────────────────────────
data class DailyRevenue(
    val date: String,
    val revenue: Double
)

// ─── Join data classes ─────────────────────────────────────────
data class RepairJobWithCustomer(
    @Embedded val repairJob: RepairJob,
    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: Customer
)

data class RepairJobFull(
    @Embedded val repairJob: RepairJob,
    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: Customer,
    @Relation(parentColumn = "id", entityColumn = "repairJobId")
    val parts: List<JobPart>
)
