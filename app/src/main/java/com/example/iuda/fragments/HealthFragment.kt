package com.example.iuda.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.iuda.MedicalData
import com.example.iuda.R
import com.example.iuda.databinding.FragmentCircleFriendsBinding
import com.example.iuda.databinding.FragmentHealthBinding
import com.example.iuda.iuda
import com.example.iuda.iuda.Companion.prefs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HealthFragment : Fragment() {
private lateinit var binding: FragmentHealthBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnRefresh = binding.btnRefreshMedicalData
        val currentUserID = prefs.getId()
        val btnSave = binding.btnSaveMedicalData

        btnRefresh.visibility = View.INVISIBLE
        // Llamar a checkExist y pasar btnSave como argumento
        checkExist(currentUserID, btnSave,btnRefresh)

        btnSave.setOnClickListener {
            saveMedicalHealth(currentUserID)
        }
    }


    private fun checkExist(currentUserID: String, btnSave: Button, btnRefresh:Button) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("MedicalData")

        databaseReference.child(currentUserID).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    getMedicalData(currentUserID)

                    btnSave.visibility = View.INVISIBLE
                    btnRefresh.visibility = View.VISIBLE

                    btnRefresh.setOnClickListener {
                        enableEditing()
                        btnRefresh.visibility = View.INVISIBLE
                        btnSave.visibility = View.VISIBLE
                    }



                } else {

                    btnSave.isEnabled = true

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar cualquier error que pueda ocurrir durante la lectura de datos
                Toast.makeText(requireContext(), "Error al verificar la existencia de datos médicos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun enableEditing() {
        // Habilitar la edición de los EditText
        binding.etBloodType.isFocusable = true
        binding.etBloodType.isFocusableInTouchMode = true

        // Repite el proceso para los demás EditText
        binding.etDiseases.isFocusable = true
        binding.etDiseases.isFocusableInTouchMode = true

        binding.etMedications.isFocusable = true
        binding.etMedications.isFocusableInTouchMode = true

        binding.etAllergies.isFocusable = true
        binding.etAllergies.isFocusableInTouchMode = true

        binding.etInsuranceNumber.isFocusable = true
        binding.etInsuranceNumber.isFocusableInTouchMode = true
    }

    private fun disableEditing() {
        // Deshabilitar la edición de los EditText
        binding.etBloodType.isFocusable = false
        binding.etBloodType.isFocusableInTouchMode = false

        // Repite el proceso para los demás EditText
        binding.etDiseases.isFocusable = false
        binding.etDiseases.isFocusableInTouchMode = false

        binding.etMedications.isFocusable = false
        binding.etMedications.isFocusableInTouchMode = false

        binding.etAllergies.isFocusable = false
        binding.etAllergies.isFocusableInTouchMode = false

        binding.etInsuranceNumber.isFocusable = false
        binding.etInsuranceNumber.isFocusableInTouchMode = false
    }

    private fun getMedicalData(currentUserID: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("MedicalData")

        databaseReference.child(currentUserID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // El usuario tiene datos médicos almacenados
                    val medicalData = snapshot.getValue(MedicalData::class.java)

                    // Mostrar los datos en los EditText correspondientes
                    binding.etBloodType.setText(medicalData?.bloodType)
                    binding.etDiseases.setText(medicalData?.diseases)
                    binding.etMedications.setText(medicalData?.medications)
                    binding.etAllergies.setText(medicalData?.allergies)
                    binding.etInsuranceNumber.setText(medicalData?.insuranceNumber)

                    // Deshabilitar la edición del EditText
                    disableEditing()

                } else {
                    // El usuario no tiene datos médicos almacenados
                    // Puedes manejar la lógica correspondiente aquí, por ejemplo, mostrar un mensaje.
                    Toast.makeText(requireContext(), "El usuario no tiene datos médicos almacenados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar cualquier error que pueda ocurrir durante la lectura de datos
                Toast.makeText(requireContext(), "Error al obtener datos médicos", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun saveMedicalHealth(currentUserID: String) {
        val sangre = binding.etBloodType.text.toString()
        val enfermedades = binding.etDiseases.text.toString()
        val medicos = binding.etMedications.text.toString()
        val alergias = binding.etAllergies.text.toString()
        val nss = binding.etInsuranceNumber.text.toString()
        val databaseReference = FirebaseDatabase.getInstance().getReference("MedicalData")
        val medicalDataUser = MedicalData(
            bloodType = sangre,
            diseases = enfermedades,
            medications = medicos,
            allergies = alergias,
            insuranceNumber = nss
        )

        databaseReference.child(currentUserID).setValue(medicalDataUser)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),"Guardado", Toast.LENGTH_SHORT).show()
                disableEditing()
                binding.btnSaveMedicalData.visibility = View.INVISIBLE
                binding.btnRefreshMedicalData.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),"Error", Toast.LENGTH_SHORT).show()
            }
    }
}
