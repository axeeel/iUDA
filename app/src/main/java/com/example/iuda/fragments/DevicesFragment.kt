package com.example.iuda.fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.iuda.R
import com.ingenieriajhr.blujhr.BluJhr


class DevicesFragment : Fragment() {
    lateinit var blue: BluJhr
    var devicesBluetooth = ArrayList<String>()
    private lateinit var listDeviceBluetooth: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_devices, container, false)

        blue = BluJhr(requireContext())
        blue.onBluetooth()
        listDeviceBluetooth = view.findViewById(R.id.listDeviceBluetooth)

        // Aquí configuras el adaptador o realizas cualquier otra configuración de vistas si es necesario

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        listDeviceBluetooth.setOnItemClickListener { adapterView, parentView, i, l ->
            if (devicesBluetooth.isNotEmpty()) {
                blue.connect(devicesBluetooth[i])
                blue.setDataLoadFinishedListener(object : BluJhr.ConnectedBluetooth {
                    override fun onConnectState(state: BluJhr.Connected) {
                        when (state) {
                            BluJhr.Connected.True -> {
                                Toast.makeText(requireActivity(), "True", Toast.LENGTH_SHORT).show()
                                //listDeviceBluetooth.visibility = View.GONE
                                //rxReceived()
                            }
                            BluJhr.Connected.Pending -> {
                                Toast.makeText(requireActivity(), "Pending", Toast.LENGTH_SHORT).show()
                            }
                            BluJhr.Connected.False -> {
                                Toast.makeText(requireContext(), "False", Toast.LENGTH_SHORT).show()
                            }
                            BluJhr.Connected.Disconnect -> {
                                Toast.makeText(requireContext(), "Disconnect", Toast.LENGTH_SHORT).show()
                                listDeviceBluetooth.visibility = View.VISIBLE
                            }
                        }
                    }
                })
            }
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // Tu lógica actual de onRequestPermissionsResult
        if (blue.checkPermissions(requestCode,grantResults)){
            Toast.makeText(requireContext(), "Exit", Toast.LENGTH_SHORT).show()
            blue.initializeBluetooth()
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                blue.initializeBluetooth()
            }else{
                Toast.makeText(requireContext(), "Algo salio mal", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Tu lógica actual de onActivityResult
        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(requireContext(), "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
