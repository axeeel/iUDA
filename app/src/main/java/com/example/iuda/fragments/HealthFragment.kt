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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
class HealthFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String // El ID del usuario actual
    private lateinit var usernamee: TextView
    private lateinit var btnSave: Button
    private lateinit var txtBloodType: EditText
    private lateinit var txtDiseases: EditText
    private lateinit var txtMedications: EditText
    private lateinit var txtAllergies: EditText
    private lateinit var txtInsuranceNumber: EditText



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val username = arguments?.getString("username")
        val root = inflater.inflate(R.layout.fragment_health, container, false)
        txtBloodType = root.findViewById(R.id.etBloodType)
        txtDiseases = root.findViewById(R.id.etDiseases)
        txtMedications = root.findViewById(R.id.etMedications)
        txtAllergies = root.findViewById(R.id.etAllergies)
        txtInsuranceNumber = root.findViewById(R.id.etInsuranceNumber)
        usernamee = root.findViewById(R.id.usernamee)
        usernamee.setText(username).toString()

        btnSave = root.findViewById(R.id.btnSaveMedicalData)
        btnSave.setOnClickListener {
            saveMedicalData()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el ID del usuario actual que se pasa como argumento
        userId = arguments?.getString("id", "userId") ?: ""

        // Inicializar la referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().reference.child("MedicalData")
    }

    private fun saveMedicalData() {
        val bloodType = txtBloodType.text.toString()
        val diseases = txtDiseases.text.toString()
        val medications = txtMedications.text.toString()
        val allergies = txtAllergies.text.toString()
        val insuranceNumber = txtInsuranceNumber.text.toString()

        val medicalData = MedicalData(
            userId,
            bloodType,
            diseases,
            medications,
            allergies,
            insuranceNumber
        )

        // Obtener una referencia única para los datos médicos del usuario actual
        val medicalDataRef = databaseReference.child(userId)

        // Guardar datos médicos en Firebase
        medicalDataRef.setValue(medicalData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datos médicos guardados exitosamente
                    Toast.makeText(
                        requireContext(),
                        "Datos médicos guardados con éxito",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Error al guardar datos médicos
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar datos médicos: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
