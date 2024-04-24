package com.radko.printer.viewholder

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.radko.printer.R
import com.radko.printer.model.PrinterManufacturer



class ManufacturerViewHolder constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    constructor(parent: ViewGroup) :
            this(
                LayoutInflater.from(parent.context).inflate(R.layout.manufacturer_item, parent, false)
            )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun bind(item: PrinterManufacturer, callback : (result: PrinterManufacturer?) -> Unit) {
        itemView.apply {
            findViewById<TextView>(R.id.name).text = item.manufacturerName
            findViewById<ImageView>(R.id.icon).setImageDrawable(itemView.context.getDrawable(item.icon))
            setOnClickListener {
                callback.invoke(item)
            }
        }
    }

}