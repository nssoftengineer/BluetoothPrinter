package com.ns

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ns.printer.*
import com.ns.printer.databinding.PrintFragmentBinding
import com.ns.printer.dialog.ChooseBTDeviceDialog
import com.ns.printer.model.PrinterManufacturer
import com.ns.printer.model.RoutePrinter
import com.ns.printer.model.prefs.Preferences
import com.ns.printer.model.prefs.PrintPreferences
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty


class PrintFragment : Fragment() {



    companion object {
        const val BARCODE_KEY: String = "BARCODE_KEY"
        const val SHOULD_DRAW_LOGO_KEY: String = "SHOULD_DRAW_LOGO_KEY"

        fun newInstance(): PrintFragment {
            return PrintFragment()
        }
    }

    private var barcode: String? = ""
    lateinit var binding: PrintFragmentBinding
    private var macChangedListener = object : Preferences.SharedPrefsListener {
        override fun onSharedPrefChanged(property: KProperty<*>) {
            if (PrintPreferences::manufacturer.name == property.name) {
                binding.connectBtn.visibility = View.GONE
                binding.printBtn.visibility = View.VISIBLE
                binding.connectivityStatusView.visibility = View.VISIBLE
                binding.connectivityStatusView.bindData { startConnectivityFlow() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PrintPreferences.getInstance(requireContext()).addListener(macChangedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        PrintPreferences.getInstance(requireContext()).removeListener(macChangedListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PrintFragmentBinding.inflate(layoutInflater, container, false);
        val view: View = binding.getRoot()
        browseBluetoothDevice()
        return view
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //  barcode = arguments?.getString(BARCODE_KEY)
        //PrintPreferences.getInstance(context!!).drawLogo = arguments?.getBoolean(SHOULD_DRAW_LOGO_KEY)
        initViews()
        checkForPermission()
    }

    @SuppressLint("CheckResult")
    private fun print() {
        binding.progressBar.visibility = View.VISIBLE
        binding.container.alpha = 0.6f
        val printer = when (PrintPreferences.getInstance(requireContext()).manufacturer) {
            PrinterManufacturer.BROTHER.manufacturerName -> BrotherPrinter.getInstance(requireContext())
            PrinterManufacturer.ZEBRA.manufacturerName -> ZebraPrinter.getInstance()
            PrinterManufacturer.CITIZEN.manufacturerName -> CitizenPrinter.getInstance()
            else -> POSPrinter.getInstance(requireContext())
        }
        Single.fromCallable {
                printer.print(barcode!!, PrintPreferences.getInstance(requireContext()).macAddress!!)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(10, TimeUnit.SECONDS)
            .subscribe(
                {
//                    if (printerType == PrinterType.BROTHER) logOutput.text =
//                        (printer as BrotherPrinter).readLog()
                    binding.progressBar.visibility = View.GONE
                    binding.container.alpha = 1f
                    showToast("Print status : $it")
                },
                {
//                    if (printerType == PrinterType.BROTHER) logOutput.text =
//                        (printer as BrotherPrinter).readLog()
                    requireActivity().runOnUiThread {
                        // Stuff that updates the UI
                        binding.progressBar.visibility = View.GONE
                        binding.container.alpha = 1f
                        showToast("Failed to print.$it")
                    }
                })
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkForPermission() {
        val PERMISSION_ALL = 1
        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!hasPermissions(
                requireContext(),
                permissions = *PERMISSIONS
            )
        ) {
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_ALL)
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun startConnectivityFlow() {
        val transaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        ChooseBTDeviceDialog()
            .show(transaction, ChooseBTDeviceDialog.TAG)
    }


    /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/
    interface OnBluetoothPermissionsGranted {
        fun onPermissionsGranted()
    }

    val PERMISSION_BLUETOOTH = 1
    val PERMISSION_BLUETOOTH_ADMIN = 2
    val PERMISSION_BLUETOOTH_CONNECT = 3
    val PERMISSION_BLUETOOTH_SCAN = 4

    var onBluetoothPermissionsGranted: OnBluetoothPermissionsGranted? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_BLUETOOTH, PERMISSION_BLUETOOTH_ADMIN, PERMISSION_BLUETOOTH_CONNECT,PERMISSION_BLUETOOTH_SCAN -> checkBluetoothPermissions(
                    onBluetoothPermissionsGranted
                )
            }
        }
    }

    fun checkBluetoothPermissions(onBluetoothPermissionsGranted: OnBluetoothPermissionsGranted?) {
        this.onBluetoothPermissionsGranted = onBluetoothPermissionsGranted
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.BLUETOOTH),
                PERMISSION_BLUETOOTH
            )
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.BLUETOOTH_ADMIN),
                PERMISSION_BLUETOOTH_ADMIN
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.BLUETOOTH_CONNECT),
                PERMISSION_BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.BLUETOOTH_SCAN),
                PERMISSION_BLUETOOTH_SCAN
            )
        } else {
            this.onBluetoothPermissionsGranted!!.onPermissionsGranted()
        }
    }

    private var selectedDevice: BluetoothConnection? = null

    fun browseBluetoothDevice() {
        checkBluetoothPermissions(object : OnBluetoothPermissionsGranted {
            override fun onPermissionsGranted() {
                val bluetoothDevicesList: Array<BluetoothConnection> =
                    BluetoothPrintersConnections().getList() as Array<BluetoothConnection>
                if (bluetoothDevicesList != null) {
                    val items =
                        arrayOfNulls<String>(bluetoothDevicesList.size + 1)
                    items[0] = "Default printer"
                    var i = 0
                    for (device in bluetoothDevicesList) {
                        items[++i] = device.getDevice().address
                    }
                    val alertDialog =
                        AlertDialog.Builder(requireActivity())
                    alertDialog.setTitle("Bluetooth printer selection")
                    alertDialog.setItems(
                        items
                    ) { dialogInterface: DialogInterface?, i1: Int ->
                        val index = i1 - 1
                        selectedDevice = if (index == -1) {
                            null
                        } else {
                            bluetoothDevicesList[index]
                        }
//                    val button =
//                        findViewById<View>(R.id.button_bluetooth_browse) as Button
//                    button.text = items[i1]
                     //   Toast.makeText(requireContext(),  items[i1], Toast.LENGTH_SHORT).show()
                    }
//                    val alert = alertDialog.create()
//                    alert.setCanceledOnTouchOutside(false)
//                    alert.show()
                }
            }
        })
    }

    private fun initViews() {
        //binding.barcodeView.bindData(barcode!!)
        binding.connectBtn.setOnClickListener {
            startConnectivityFlow()
        }
        binding.printBtn.setOnClickListener {
            print()
        }
        binding.printBtn2.setOnClickListener {
            printImage()
        }
        if (!PrintPreferences.getInstance(requireContext()).macAddress.isNullOrEmpty() &&
            !PrintPreferences.getInstance(requireContext()).deviceName.isNullOrEmpty() &&
            !PrintPreferences.getInstance(requireContext()).manufacturer.isNullOrEmpty()) {
            binding.connectBtn.visibility = View.GONE
            binding.printBtn.visibility = View.VISIBLE
            binding.printBtn2.visibility = View.VISIBLE
            binding.connectivityStatusView.visibility = View.VISIBLE
            binding.connectivityStatusView.bindData { startConnectivityFlow() }
        }
    }

    private fun printImage() {
        val content: LinearLayout = binding.container
        content.isDrawingCacheEnabled = true
        val bitmap: Bitmap = content.getDrawingCache()

        val printer: RoutePrinter

        printer = ZebraPrinter.getInstance()
        Single.fromCallable {
            printer.print(bitmap!!, PrintPreferences.getInstance(requireContext()).macAddress!!)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(10, TimeUnit.SECONDS)
            .subscribe(
                {
                    binding.progressBar.visibility = View.GONE
                    binding.container.alpha = 1f
                    showToast("Print status : $it")
                },
                {

                    requireActivity().runOnUiThread {
                        // Stuff that updates the UI
                        binding.progressBar.visibility = View.GONE
                        binding.container.alpha = 1f
                        showToast("Failed to print.$it")
                    }
                })

    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}