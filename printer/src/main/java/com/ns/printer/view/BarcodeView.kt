package com.ns.printer.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.ns.printer.R
import com.ns.printer.databinding.BarcodeViewLayoutBinding


class BarcodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG: String = "BarcodeView"
        private const val WIDTH: Int = 250
        private const val HEIGHT: Int = 100
    }
    lateinit var binding: BarcodeViewLayoutBinding
    init {
        LayoutInflater.from(context).inflate(R.layout.barcode_view_layout, this, true)
    }

    fun bindData(number: String) {
        generateBarcode(number)
//        value.text = number
//        if (PrintPreferences.getInstance(context!!).drawLogo!!) {
//            logo.visibility = View.VISIBLE
//        }
    }

    private fun generateBarcode(number: String) {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(number, BarcodeFormat.CODABAR, WIDTH, HEIGHT)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            //image.setImageBitmap(bitmap)
        } catch (exception: WriterException) {
            Log.e(TAG, "Error has been occured while trying to generate barcode image.", exception)
            exception.printStackTrace()
        }
    }
}