package com.ns.printer.viewholder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ns.printer.R
import com.ns.printer.model.BTPrinter



class BTDeviceViewHolder constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    constructor(parent: ViewGroup) :
            this(
                LayoutInflater.from(parent.context).inflate(R.layout.bt_device_item, parent, false)
            )

    fun bind(item: BTPrinter, callback : (result: BTPrinter?) -> Unit) {
        Log.d("TAG", "bind: "+item.name+item.macAddress)
        itemView.apply {
            findViewById<TextView>(R.id.name).text = item.name
          //  findViewById<TextView>(R.id.macAddress).text = item.macAddress
            setOnClickListener {
                callback.invoke(item)
            }
        }
    }

}