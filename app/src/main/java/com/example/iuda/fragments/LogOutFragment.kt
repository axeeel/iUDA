package com.example.iuda.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.iuda.R
import com.example.iuda.iuda.Companion.prefs

@SuppressLint("StaticFieldLeak")
private lateinit var btn_CerrarSesion:Button

class LogOutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_log_out, container, false)
         btn_CerrarSesion = root.findViewById(R.id.btn_CerrarSesion)


        return root
    }

}