package com.secondlife.mobile.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.secondlife.mobile.data.Bill
import com.secondlife.mobile.data.Customer
import com.secondlife.mobile.data.RepairJob
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfBillHelper {

    fun generateAndShare(context: Context, bill: Bill, job: RepairJob, customer: Customer) {
        val file = generatePdf(context, bill, job, customer)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Bill from Second Life Mobiles - ${bill.billNumber}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Bill via"))
    }

    fun shareViaWhatsApp(context: Context, bill: Bill, job: RepairJob, customer: Customer) {
        val file = generatePdf(context, bill, job, customer)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val wa = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(wa)
        } catch (e: Exception) {
            generateAndShare(context, bill, job, customer)
        }
    }

    private fun generatePdf(context: Context, bill: Bill, job: RepairJob, customer: Customer): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        drawBill(page.canvas, bill, job, customer)
        pdfDoc.finishPage(page)
        val dir = File(context.cacheDir, "bills")
        dir.mkdirs()
        val file = File(dir, "${bill.billNumber}.pdf")
        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()
        return file
    }

    // BUG 11 FIX: text wrapping helper — splits long text to max width
    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Float): Float {
        var currentY = y
        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, x, currentY, paint)
                currentY += paint.textSize + 4f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, x, currentY, paint)
            currentY += paint.textSize + 4f
        }
        return currentY
    }

    private fun drawBill(canvas: Canvas, bill: Bill, job: RepairJob, customer: Customer) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val warrantyDate = sdf.format(Date(bill.warrantyExpiry))
        val billDate = sdf.format(Date(bill.createdAt))

        val limeGreen = Color.parseColor("#84CC16")
        val darkBlue = Color.parseColor("#1565C0")
        val white = Color.WHITE
        val black = Color.BLACK
        val grey = Color.parseColor("#666666")
        val lightGrey = Color.parseColor("#F5F5F5")

        val pHeader = Paint().apply { color = limeGreen }
        val pText = Paint().apply { color = black; textSize = 13f; isAntiAlias = true }
        val pSmall = Paint().apply { color = grey; textSize = 11f; isAntiAlias = true }
        val pBold = Paint().apply { color = black; textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
        val pWhite = Paint().apply { color = white; textSize = 18f; isFakeBoldText = true; isAntiAlias = true }
        val pWhiteSmall = Paint().apply { color = white; textSize = 12f; isAntiAlias = true }
        val pLine = Paint().apply { color = limeGreen; strokeWidth = 2f }

        canvas.drawRect(0f, 0f, 595f, 120f, pHeader)
        canvas.drawText("SECOND LIFE MOBILES", 30f, 55f, pWhite)
        canvas.drawText("Tested. Trusted. Twice as Smart.", 30f, 80f, pWhiteSmall)
        canvas.drawText(bill.branch, 30f, 105f, pWhiteSmall)

        val pBillNum = Paint().apply { color = white; textSize = 13f; textAlign = Paint.Align.RIGHT; isAntiAlias = true }
        canvas.drawText("BILL: ${bill.billNumber}", 565f, 55f, pBillNum)
        canvas.drawText("Date: $billDate", 565f, 75f, pBillNum)
        canvas.drawText("Payment: ${bill.paymentMode}", 565f, 95f, pBillNum)

        canvas.drawLine(30f, 135f, 565f, 135f, pLine)

        var y = 160f
        canvas.drawText("BILL TO", 30f, y, pBold.apply { textSize = 13f })
        y += 20f
        // BUG 11 FIX: wrap customer name and address if long
        y = drawWrappedText(canvas, customer.name, 30f, y, pText.apply { textSize = 13f }, 250f)
        y = drawWrappedText(canvas, "📞 ${customer.phone}", 30f, y, pSmall, 250f)
        if (customer.address.isNotEmpty()) {
            y = drawWrappedText(canvas, customer.address, 30f, y, pSmall, 250f)
        }

        val pRight = Paint().apply { color = black; textSize = 12f; textAlign = Paint.Align.RIGHT; isAntiAlias = true }
        val pRightBold = Paint().apply { color = black; textSize = 13f; textAlign = Paint.Align.RIGHT; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("DEVICE", 565f, 160f, pRightBold)
        // BUG 11 FIX: clamp device string to 40 chars; wrap imei
        val deviceStr = "${job.deviceBrand} ${job.deviceModel}".take(40)
        canvas.drawText(deviceStr, 565f, 178f, pRight)
        if (job.imei.isNotEmpty()) canvas.drawText("IMEI: ${job.imei}".take(35), 565f, 196f, pRight)
        canvas.drawText("Job #${job.id}", 565f, 214f, pRight)

        y = 240f.coerceAtLeast(y + 10f)
        canvas.drawRect(30f, y - 18f, 565f, y + 5f, Paint().apply { color = darkBlue })
        val pTHRight = Paint().apply { color = white; textSize = 12f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT; isAntiAlias = true }
        canvas.drawText("DESCRIPTION", 40f, y, Paint().apply { color = white; textSize = 12f; isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("AMOUNT", 555f, y, pTHRight)
        y += 20f

        fun tableRow(label: String, value: String, highlight: Boolean = false) {
            if (highlight) canvas.drawRect(30f, y - 14f, 565f, y + 6f, Paint().apply { color = lightGrey })
            canvas.drawText(label.take(50), 40f, y, pText.apply { textSize = 12f })
            canvas.drawText(value, 555f, y, Paint().apply { color = black; textSize = 12f; textAlign = Paint.Align.RIGHT; isAntiAlias = true })
            y += 22f
        }

        tableRow("Labour Charge", "₹${String.format("%.2f", bill.laborCharge)}", true)
        tableRow("Spare Parts", "₹${String.format("%.2f", bill.partsCharge)}")
        if (bill.discount > 0) tableRow("Discount", "-₹${String.format("%.2f", bill.discount)}", true)

        canvas.drawLine(30f, y, 565f, y, pLine)
        y += 20f

        canvas.drawRect(30f, y - 18f, 565f, y + 8f, Paint().apply { color = limeGreen })
        canvas.drawText("TOTAL", 40f, y, Paint().apply { color = black; textSize = 15f; isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("₹${String.format("%.2f", bill.totalAmount)}", 555f, y, Paint().apply { color = black; textSize = 15f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT; isAntiAlias = true })
        y += 28f

        tableRow("Amount Paid", "₹${String.format("%.2f", bill.amountPaid)}", true)
        tableRow("Balance Due", "₹${String.format("%.2f", bill.totalAmount - bill.amountPaid)}")

        y += 10f
        canvas.drawLine(30f, y, 565f, y, Paint().apply { color = lightGrey; strokeWidth = 1f })
        y += 20f
        // BUG 11 FIX: wrap issue description
        val issueLabel = Paint().apply { color = grey; textSize = 11f; isAntiAlias = true }
        y = drawWrappedText(canvas, "Issue: ${job.issueDescription}", 30f, y, issueLabel, 520f)

        // Warranty box — pin to safe Y position
        val warrantyY = (y + 20f).coerceAtMost(730f)
        canvas.drawRect(30f, warrantyY, 565f, warrantyY + 60f, Paint().apply { color = Color.parseColor("#E8F5E9") })
        canvas.drawRect(30f, warrantyY, 32f, warrantyY + 60f, Paint().apply { color = limeGreen })
        canvas.drawText("🛡️  90-Day Warranty", 45f, warrantyY + 22f, pBold.apply { textSize = 13f })
        canvas.drawText("Valid until: $warrantyDate", 45f, warrantyY + 42f, pSmall)

        // Footer
        val footerSmall = Paint().apply { color = grey; textSize = 10f; textAlign = Paint.Align.CENTER; isAntiAlias = true }
        canvas.drawText("Thank you for choosing Second Life Mobiles  •  Mimisal & R.Puduppattinam, Tamil Nadu", 297f, 820f, footerSmall)
    }
}
