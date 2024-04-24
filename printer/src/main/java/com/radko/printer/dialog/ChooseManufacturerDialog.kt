package com.radko.printer.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.radko.printer.R
import com.radko.printer.adapter.ManufacturerAdapter
import com.radko.printer.databinding.ChooseManufacturerDialogLayoutBinding
import com.radko.printer.model.PrinterManufacturer
import com.radko.printer.model.prefs.PrintPreferences


class ChooseManufacturerDialog : DialogFragment() {

    companion object {
        const val TAG: String = "ChooseManufacturerDialog"
    }
    lateinit var binding: ChooseManufacturerDialogLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChooseManufacturerDialogLayoutBinding.inflate(layoutInflater, container, false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initManufacturersList()
    }

    private fun initManufacturersList() {
        val adapter = ManufacturerAdapter {
            PrintPreferences.getInstance(requireContext()).manufacturer = it!!.manufacturerName
            dismiss()
        }
        adapter.setManufacturersList(PrinterManufacturer.values().toList())
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val horizontalDecoration = DividerItemDecoration(
            binding.manufacturerList.context,
            DividerItemDecoration.VERTICAL
        )
        val horizontalDivider =
            ContextCompat.getDrawable(requireActivity(), R.drawable.horizontal_divider)
        horizontalDecoration.setDrawable(horizontalDivider!!)
        binding.manufacturerList.addItemDecoration(horizontalDecoration)
        binding.manufacturerList.layoutManager = layoutManager
        binding.manufacturerList.adapter = adapter
    }
}