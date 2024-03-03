package com.example.iuda.fragments

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.example.iuda.MainActivity
import com.example.iuda.R
import com.ingenieriajhr.blujhr.BluJhr

class DeviceActivity : AppCompatActivity() {
    lateinit var blue: BluJhr
    var devicesBluetooth = ArrayList<String>()
    private lateinit var listDeviceBluetooth: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        blue = BluJhr(this)
        blue.onBluetooth()
        listDeviceBluetooth = findViewById(R.id.listDeviceBluetooth)

        listDeviceBluetooth.setOnItemClickListener { adapterView, parentview, i, l ->
            if (devicesBluetooth.isNotEmpty()) {
                blue.connect(devicesBluetooth[i])
                blue.setDataLoadFinishedListener(object : BluJhr.ConnectedBluetooth {
                    override fun onConnectState(state: BluJhr.Connected) {
                        when (state) {

                            BluJhr.Connected.True -> {
                                Toast.makeText(applicationContext, "True", Toast.LENGTH_SHORT)
                                    .show()
                                val intent = Intent(this@DeviceActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()

                                //rxReceived()
                            }

                            BluJhr.Connected.Pending -> {
                                Toast.makeText(applicationContext, "Pending", Toast.LENGTH_SHORT)
                                    .show()

                            }

                            BluJhr.Connected.False -> {
                                Toast.makeText(applicationContext, "False", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            BluJhr.Connected.Disconnect -> {
                                Toast.makeText(applicationContext, "Disconnect", Toast.LENGTH_SHORT)
                                    .show()
                                listDeviceBluetooth.visibility = View.VISIBLE
                                //viewConn.visibility = View.GONE
                            }

                        }
                    }
                })
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (blue.checkPermissions(requestCode,grantResults)){
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            blue.initializeBluetooth()
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                blue.initializeBluetooth()
            }else{
                Toast.makeText(this, "Algo salio mal", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!blue.stateBluetoooth() && requestCode == 100){
            blue.initializeBluetooth()
        }else{
            if (requestCode == 100){
                devicesBluetooth = blue.deviceBluetooth()
                if (devicesBluetooth.isNotEmpty()){
                    val adapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,devicesBluetooth)
                    listDeviceBluetooth.adapter = adapter
                }else{
                    Toast.makeText(this, "No tienes vinculados dispositivos", Toast.LENGTH_SHORT).show()
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}