package com.ns.printer.model

import android.graphics.Bitmap

interface RoutePrinter {
    fun print(barcode: String, macAddress: String): PrintStatus
    fun print(barcode: Bitmap, macAddress: String): PrintStatus
}