package com.secondlife.mobile.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.secondlife.mobile.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object BackupRestoreHelper {

    suspend fun exportBackup(context: Context): Uri? = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val root = JSONObject()
            root.put("version", 2)
            root.put("exportedAt", System.currentTimeMillis())
            root.put("branch", "Second Life Mobiles Backup")

            // BUG 7 FIX: actually query and export all data
            // Customers
            val custArr = JSONArray()
            db.customerDao().getAllCustomersList().forEach { c ->
                custArr.put(JSONObject().apply {
                    put("id", c.id); put("name", c.name); put("phone", c.phone)
                    put("email", c.email); put("address", c.address)
                    put("branch", c.branch); put("createdAt", c.createdAt)
                })
            }
            root.put("customers", custArr)

            // Technicians
            val techArr = JSONArray()
            db.technicianDao().getAllTechniciansList().forEach { t ->
                techArr.put(JSONObject().apply {
                    put("id", t.id); put("name", t.name); put("phone", t.phone)
                    put("specialty", t.specialty); put("branch", t.branch)
                    put("isActive", t.isActive); put("createdAt", t.createdAt)
                })
            }
            root.put("technicians", techArr)

            // Repair Jobs
            val jobArr = JSONArray()
            db.repairJobDao().getAllRepairJobsList().forEach { j ->
                jobArr.put(JSONObject().apply {
                    put("id", j.id); put("customerId", j.customerId)
                    put("technicianId", j.technicianId)
                    put("deviceBrand", j.deviceBrand); put("deviceModel", j.deviceModel)
                    put("imei", j.imei); put("issueDescription", j.issueDescription)
                    put("status", j.status); put("estimatedCost", j.estimatedCost)
                    put("finalCost", j.finalCost); put("advancePaid", j.advancePaid)
                    put("branch", j.branch); put("createdAt", j.createdAt)
                })
            }
            root.put("repairJobs", jobArr)

            // Spare Parts
            val partArr = JSONArray()
            db.sparePartDao().getAllSparePartsList().forEach { p ->
                partArr.put(JSONObject().apply {
                    put("id", p.id); put("name", p.name); put("category", p.category)
                    put("quantity", p.quantity); put("buyingPrice", p.buyingPrice)
                    put("sellingPrice", p.sellingPrice); put("minStock", p.minStock)
                    put("branch", p.branch)
                })
            }
            root.put("spareParts", partArr)

            // Bills
            val billArr = JSONArray()
            db.billDao().getAllBillsList().forEach { b ->
                billArr.put(JSONObject().apply {
                    put("id", b.id); put("repairJobId", b.repairJobId)
                    put("billNumber", b.billNumber); put("laborCharge", b.laborCharge)
                    put("partsCharge", b.partsCharge); put("discount", b.discount)
                    put("totalAmount", b.totalAmount); put("amountPaid", b.amountPaid)
                    put("paymentMode", b.paymentMode); put("branch", b.branch)
                    put("warrantyExpiry", b.warrantyExpiry); put("createdAt", b.createdAt)
                })
            }
            root.put("bills", billArr)

            // Used Phones
            val phoneArr = JSONArray()
            db.usedPhoneDao().getAllUsedPhonesList().forEach { p ->
                phoneArr.put(JSONObject().apply {
                    put("id", p.id); put("brand", p.brand); put("model", p.model)
                    put("imei", p.imei); put("grade", p.grade); put("storage", p.storage)
                    put("ram", p.ram); put("color", p.color); put("condition", p.condition)
                    put("buyingPrice", p.buyingPrice); put("sellingPrice", p.sellingPrice)
                    put("status", p.status); put("branch", p.branch)
                    put("soldAt", p.soldAt); put("buyerId", p.buyerId)
                    put("createdAt", p.createdAt)
                })
            }
            root.put("usedPhones", phoneArr)

            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
            val fileName = "SLM_Backup_${sdf.format(Date())}.json"
            val dir = File(context.cacheDir, "backups")
            dir.mkdirs()
            val file = File(dir, fileName)
            file.writeText(root.toString(2))

            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareBackup(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Second Life Mobiles Backup")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Save Backup via"))
    }
}
