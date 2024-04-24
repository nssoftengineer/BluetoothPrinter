package com.ns.bluetooth_printer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ns.PrintFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateToPrint()
    }

    private fun navigateToPrint() {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        val printFragment: Fragment = PrintFragment.newInstance()
        transaction.replace(R.id.container, printFragment)
        transaction.commit()
    }

}