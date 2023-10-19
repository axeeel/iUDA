package com.example.iuda

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.iuda.databinding.ActivityUploadBinding
import com.example.iuda.fragments.HealthFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UploadActivity : AppCompatActivity() {
    // Declaración de variables
    private lateinit var binding: ActivityUploadBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el diseño de la actividad y establecerlo como contenido de la actividad
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar la instancia de FirebaseDatabase y obtener una referencia a la ubicación "Users"
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("Users")

        // Configurar el Spinner con opciones y adaptador
        val genderOptions = arrayOf("Masculino", "Femenino", "Otro")
        val genderAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.uploadSpinnerGender.adapter = genderAdapter

        // Configurar un listener para el botón de guardar
        binding.saveButton.setOnClickListener {
            // Obtener los valores de los campos de entrada
            val username = binding.uploadUsername.text.toString()
            val name = binding.uploadName.text.toString()
            val email = binding.uploadEmail.text.toString()
            val phone = binding.uploadNumberPhone.text.toString()
            val password = binding.uploadPassword.text.toString()

            // Obtener la posición seleccionada en el Spinner y determinar el género seleccionado
            val selectedGenderPosition = binding.uploadSpinnerGender.selectedItemPosition
            val gender = genderOptions[selectedGenderPosition]

            // Verificar si todos los campos obligatorios están completos
            if (username.isNotEmpty() && name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                //Aqui ira la funcion de autenticacion
                AuthUser(email, password)
                // Llamar a la función para registrar al usuario
                signupUser(username, name, email, phone, password, gender)


            } else {
                // Mostrar un mensaje de advertencia si faltan datos
                Toast.makeText(this@UploadActivity, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun AuthUser(email: String, password: String){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
    }
    // Función para registrar al usuario en la base de datos
    private fun signupUser(username: String, fullname: String, email: String, phone: String, password: String, gender: String) {
        // Realizar una consulta para verificar si ya existe un usuario con el mismo nombre de usuario
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                // Manejar los datos obtenidos después de la consulta
                if (!datasnapshot.exists()) {
                    // Si el nombre de usuario no existe, generar una nueva clave única y guardar el usuario en la base de datos
                    val id = databaseReference.push().key
                    val userData = UserProfile(id, username, fullname, email, phone, password, gender)
                    databaseReference.child(id!!).setValue(userData)

                    // Mostrar un mensaje de éxito y navegar a la actividad de inicio de sesión
                    Toast.makeText(this@UploadActivity, "Resgistro exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@UploadActivity, LogInActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Mostrar un mensaje si el usuario ya existe y limpiar el campo de nombre de usuario
                    Toast.makeText(this@UploadActivity, "Ya existe ese nombre de usuario", Toast.LENGTH_SHORT).show()
                    binding.uploadUsername.text.clear()
                }
            }

            // Manejar los casos en que se produce un error en la lectura de datos desde la base de datos
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UploadActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
