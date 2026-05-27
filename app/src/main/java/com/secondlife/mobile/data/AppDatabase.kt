package com.secondlife.mobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Customer::class, Technician::class, RepairJob::class,
                SparePart::class, JobPart::class, Bill::class, UsedPhone::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun technicianDao(): TechnicianDao
    abstract fun repairJobDao(): RepairJobDao
    abstract fun sparePartDao(): SparePartDao
    abstract fun jobPartDao(): JobPartDao
    abstract fun billDao(): BillDao
    abstract fun usedPhoneDao(): UsedPhoneDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Migration from v1 → v2: add branch, technician, usedPhone, jobParts etc.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add branch to existing tables
                db.execSQL("ALTER TABLE customers ADD COLUMN branch TEXT NOT NULL DEFAULT 'Mimisal'")
                db.execSQL("ALTER TABLE repair_jobs ADD COLUMN branch TEXT NOT NULL DEFAULT 'Mimisal'")
                db.execSQL("ALTER TABLE repair_jobs ADD COLUMN technicianId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE repair_jobs ADD COLUMN warrantyDays INTEGER NOT NULL DEFAULT 90")
                db.execSQL("ALTER TABLE spare_parts ADD COLUMN branch TEXT NOT NULL DEFAULT 'Mimisal'")
                db.execSQL("ALTER TABLE bills ADD COLUMN branch TEXT NOT NULL DEFAULT 'Mimisal'")
                db.execSQL("ALTER TABLE bills ADD COLUMN warrantyExpiry INTEGER NOT NULL DEFAULT 0")

                // New tables
                db.execSQL("""CREATE TABLE IF NOT EXISTS technicians (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL DEFAULT '',
                    specialty TEXT NOT NULL DEFAULT '',
                    branch TEXT NOT NULL DEFAULT 'Mimisal',
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL)""")

                db.execSQL("""CREATE TABLE IF NOT EXISTS job_parts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    repairJobId INTEGER NOT NULL,
                    sparePartId INTEGER NOT NULL DEFAULT 0,
                    partName TEXT NOT NULL,
                    quantity INTEGER NOT NULL DEFAULT 1,
                    unitPrice REAL NOT NULL DEFAULT 0.0,
                    FOREIGN KEY(repairJobId) REFERENCES repair_jobs(id) ON DELETE CASCADE,
                    FOREIGN KEY(sparePartId) REFERENCES spare_parts(id) ON DELETE SET DEFAULT)""")

                db.execSQL("""CREATE TABLE IF NOT EXISTS used_phones (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    brand TEXT NOT NULL,
                    model TEXT NOT NULL,
                    imei TEXT NOT NULL DEFAULT '',
                    grade TEXT NOT NULL DEFAULT 'A',
                    storage TEXT NOT NULL DEFAULT '',
                    ram TEXT NOT NULL DEFAULT '',
                    color TEXT NOT NULL DEFAULT '',
                    condition TEXT NOT NULL DEFAULT '',
                    buyingPrice REAL NOT NULL DEFAULT 0.0,
                    sellingPrice REAL NOT NULL DEFAULT 0.0,
                    status TEXT NOT NULL DEFAULT 'In Stock',
                    branch TEXT NOT NULL DEFAULT 'Mimisal',
                    soldAt INTEGER,
                    buyerId INTEGER,
                    createdAt INTEGER NOT NULL)""")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "slm_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
            }
        }
    }
}
