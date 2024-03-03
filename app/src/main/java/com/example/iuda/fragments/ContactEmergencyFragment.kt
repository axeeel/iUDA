package com.example.iuda.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.iuda.R
import com.example.iuda.databinding.FragmentContactEmergencyBinding
import com.example.iuda.iuda.Companion.prefs

class ContactEmergencyFragment : Fragment() {
    private lateinit var binding: FragmentContactEmergencyBinding
    private lateinit var nameCon:String
    private lateinit var phoneCon:String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactEmergencyBinding.inflate(inflater,container,false)
        return binding.root


        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnAdd = binding.btnAdd
        nameCon = prefs.getNameContact()
        phoneCon = prefs.getPhoneContact()

        if(nameCon != null && phoneCon!=null){
            completeFields()
        }
        if(nameCon == "" && phoneCon == ""){
            Log.d("prefs","No hay preferencias guardadas")
            checkVisibility()
        }



    }

    private fun fillForm() {
        binding.btnAdd.visibility = View.GONE
        binding.lnyForm.visibility = View.VISIBLE
        nameCon = binding.cnName.text.toString()
        phoneCon = binding.cnPhone.text.toString()
        binding.btnSaveMedicalData.setOnClickListener {

            prefs.saveNameContact(nameCon)
            prefs.saveNumberContact(phoneCon)
        }
    }

    private fun checkVisibility() {
        val form = binding.lnyForm

        form.visibility = View.GONE
        binding.btnAdd.visibility = View.VISIBLE

        binding.btnAdd.setOnClickListener {
            fillForm()
        }
    }

    private fun completeFields() {
        binding.cnName.setText(nameCon)
        binding.cnPhone.setText(phoneCon)
        binding.btnAdd.visibility = View.GONE

        binding.btnSaveMedicalData.setOnClickListener {

        }
    }

}