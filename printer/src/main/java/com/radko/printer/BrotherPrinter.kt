package com.radko.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.brother.ptouch.sdk.Printer
import com.brother.ptouch.sdk.PrinterInfo
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.radko.printer.model.PrintStatus
import com.radko.printer.model.RoutePrinter
import com.radko.printer.model.SingletonHolder
import com.radko.printer.model.status.BrotherStatus
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "BrotherPrinter"

class BrotherPrinter(private val context: Context) : RoutePrinter {

    companion object : SingletonHolder<BrotherPrinter, Context>({
        BrotherPrinter(it)
    })

    override fun print(barcode: String, macAddress: String): PrintStatus {
        var paperInfoFile: File
        // define printer and printer setting information
        val printer = Printer().apply {
            printerInfo = PrinterInfo().apply {
                printerModel = PrinterInfo.Model.RJ_4030
                port = PrinterInfo.Port.BLUETOOTH
                val wrapper = ContextWrapper(context)
                paperInfoFile = wrapper.getDir("PaperData", Context.MODE_PRIVATE)
                paperInfoFile = File(paperInfoFile, "${UUID.randomUUID()}.bin")
                val inputStream: InputStream = context.resources.openRawResource(
                    context.resources.getIdentifier(
                        "rj4030_102mm",
                        "raw", context.packageName
                    )
                )
                copyInputStreamToFile(inputStream, paperInfoFile)
                customPaper = paperInfoFile.absolutePath
                this.macAddress = macAddress
            }
        }

        // Pass Bluetooth adapter to the library (Bluetooth only)
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        printer.setBluetooth(bluetoothAdapter)

        try {
            //print
            printer.startCommunication()
            // Brother SDK accepts bitmap as input so barcode to bitmap conversion should be performed
            val bitmap = generateBarcodeBitmap(barcode) ?: return PrintStatus(false, "Failed to generate barcode bitmap.")
            val status = printer.printImage(bitmap)
            paperInfoFile.delete()
            return PrintStatus(status.errorCode == PrinterInfo.ErrorCode.ERROR_NONE,
                BrotherStatus(status.errorCode).getMessage())
        } finally {
            printer.endCommunication()
        }
    }

    override fun print(barcode: Bitmap, macAddress: String): PrintStatus {
        TODO("Not yet implemented")
    }

    @SuppressLint("SimpleDateFormat")
    fun readLog(): String {
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd")
        val file = File(
            Environment.getExternalStorageDirectory().path + "/Brother" + File.separator + "CM" + simpleDateFormat.format(
                Date()
            ) + ".txt"
        )
        //Read text from file
        val text = StringBuilder()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error while trying to read log file.", e)
        }
        return text.toString()
    }

    private fun generateBarcodeBitmap(input: String): Bitmap? {
        return try {
            MultiFormatWriter().encode(input, BarcodeFormat.CODABAR, 800, 200)
                .let(BarcodeEncoder()::createBitmap)
        } catch (e: WriterException) {
            Log.e(TAG, "Error has been occurred while trying to generate barcode bitmap.", e)
            null
        }
    }

    // Copy an InputStream to a File.
    private fun copyInputStreamToFile(`in`: InputStream, file: File): Boolean {
        var out: OutputStream? = null
        try {
            out = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error has been occurred while trying to copy input stream to file.", e)
            return false
        } finally { // Ensure that the InputStreams are closed even if there's an exception.
            try {
                out?.close()
                `in`.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error has been occurred while trying to close input stream.", e)
            }
        }
        return true
    }

}