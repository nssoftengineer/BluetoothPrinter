package com.ns.printer.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.ns.printer.R
import com.ns.printer.model.prefs.PrintPreferences


class ConnectionStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.connection_status_view_layout, this, true)
    }

    fun bindData(callback : (result: Any?) -> Unit) {
        val preferences = PrintPreferences.getInstance(context)
        findViewById<TextView>(R.id.name).text = preferences.deviceName;
        findViewById<TextView>(R.id.macAddress).text = preferences.macAddress
        findViewById<TextView>(R.id.manufacturer).text = preferences.manufacturer
        findViewById<com.ns.printer.view.BaseTextView>(R.id.reconnectBtn).setOnClickListener {
            callback.invoke(it)
        }
    }

}